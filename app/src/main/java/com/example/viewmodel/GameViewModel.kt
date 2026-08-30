package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdConfig
import com.example.ads.BannerCreative
import com.example.ads.TestAdManager
import com.example.ads.TestAdState
import com.example.data.db.GameDatabase
import com.example.data.db.GameRepository
import com.example.data.db.LevelProgressEntity
import com.example.data.db.UserPreferencesEntity
import com.example.engine.GameAudioSynthesizer
import com.example.engine.LevelCatalog
import com.example.engine.PuzzleSolver
import com.example.model.ArrowItem
import com.example.model.FloatingTextPopup
import com.example.model.GameStatus
import com.example.model.InputState
import com.example.model.LevelDefinition
import com.example.model.LevelResult
import com.example.model.SparkParticle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random

data class GameUiState(
  val level: LevelDefinition = LevelCatalog.getLevel(1),
  val arrows: List<ArrowItem> = emptyList(),
  val lives: Int = 3,
  val moves: Int = 0,
  val comboStreak: Int = 0,
  val timerSeconds: Int = 0,
  val gameStatus: GameStatus = GameStatus.PLAYING,
  val inputState: InputState = InputState.IDLE,
  val highlightedHintArrowId: Int? = null,
  val particles: List<SparkParticle> = emptyList(),
  val floatingPopups: List<FloatingTextPopup> = emptyList(),
  val boardShakeTrigger: Long = 0L,
  val result: LevelResult? = null,
  val isAdShowing: Boolean = false,
  val isInterstitialShowing: Boolean = false,
  val adSecondsRemaining: Int = 0,
  val adSponsor: String = "",
  val adMessage: String? = null,
  val bannerVisible: Boolean = true,
  val hintTickets: Int = 5,
  val soundEnabled: Boolean = true,
  val vibrationEnabled: Boolean = true,
  val colorblindMode: Boolean = false,
  val bestMovesForLevel: Int = 0,
  val bestStarsForLevel: Int = 0
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: GameRepository
  val adManager = TestAdManager.instance

  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  val bannerCreative: StateFlow<BannerCreative> = adManager.activeBannerCreative

  private var timerJob: Job? = null
  private val random = Random()

  val userPreferences: StateFlow<UserPreferencesEntity>

  init {
    GameAudioSynthesizer.init(application)
    val database = GameDatabase.getDatabase(application)
    repository = GameRepository(database.levelProgressDao())
    userPreferences = repository.userPreferences.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserPreferencesEntity()
    )

    // Ensure level 1 is unlocked initially
    viewModelScope.launch {
      repository.unlockLevel(1)
    }

    // Sync preferences into uiState
    viewModelScope.launch {
      userPreferences.collect { prefs ->
        _uiState.update {
          it.copy(
            hintTickets = prefs.hintTickets,
            soundEnabled = prefs.soundEnabled,
            vibrationEnabled = prefs.vibrationEnabled,
            colorblindMode = prefs.colorblindMode
          )
        }
      }
    }

    // Sync banner visibility
    viewModelScope.launch {
      adManager.bannerVisible.collect { visible ->
        _uiState.update { it.copy(bannerVisible = visible) }
      }
    }

    // Sync interstitial state
    viewModelScope.launch {
      adManager.interstitialState.collect { state ->
        when (state) {
          is TestAdState.Showing -> {
            _uiState.update {
              it.copy(
                isInterstitialShowing = true,
                adSecondsRemaining = state.secondsRemaining,
                adSponsor = state.sponsorName
              )
            }
          }
          is TestAdState.Idle -> {
            _uiState.update { it.copy(isInterstitialShowing = false) }
          }
          else -> {}
        }
      }
    }

    // Load initial level 1
    loadLevel(1)
  }

  fun loadLevel(levelId: Int) {
    timerJob?.cancel()
    val levelDef = LevelCatalog.getLevel(levelId)
    val arrows = levelDef.arrows.map { it.toArrowItem() }

    viewModelScope.launch {
      val progress = repository.getProgressForLevel(levelId)
      _uiState.update {
        it.copy(
          level = levelDef,
          arrows = arrows,
          lives = levelDef.startingLives,
          moves = 0,
          comboStreak = 0,
          timerSeconds = 0,
          gameStatus = GameStatus.PLAYING,
          inputState = InputState.IDLE,
          highlightedHintArrowId = null,
          particles = emptyList(),
          floatingPopups = emptyList(),
          boardShakeTrigger = 0L,
          result = null,
          isAdShowing = false,
          adMessage = null,
          bestMovesForLevel = progress?.bestMoves ?: 0,
          bestStarsForLevel = progress?.starsEarned ?: 0
        )
      }
      startTimer()
    }
  }

  private fun startTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (_uiState.value.gameStatus == GameStatus.PLAYING) {
        delay(1000)
        if (_uiState.value.gameStatus == GameStatus.PLAYING && !_uiState.value.isAdShowing) {
          _uiState.update { it.copy(timerSeconds = it.timerSeconds + 1) }
        }
      }
    }
  }

  fun resetLevel() {
    GameAudioSynthesizer.playButtonClick(_uiState.value.soundEnabled)
    val currentLevelId = _uiState.value.level.levelId
    loadLevel(currentLevelId)
  }

  fun nextLevel() {
    GameAudioSynthesizer.playButtonClick(_uiState.value.soundEnabled)
    val nextId = _uiState.value.level.levelId + 1
    if (nextId <= LevelCatalog.TOTAL_LEVELS) {
      loadLevel(nextId)
    }
  }

  fun onArrowClicked(arrowId: Int) {
    val state = _uiState.value
    // Protect against double taps, multi-touch, or taps during animation/finished state
    if (state.inputState != InputState.IDLE || state.gameStatus != GameStatus.PLAYING) {
      return
    }

    val arrow = state.arrows.find { it.id == arrowId && it.isActive } ?: return

    _uiState.update { it.copy(inputState = InputState.VALIDATING) }

    val (isClear, blocker) = PuzzleSolver.checkPathClear(
      arrow = arrow,
      allActiveArrows = state.arrows.filter { it.isActive },
      rows = state.level.rows,
      cols = state.level.cols
    )

    if (isClear) {
      handleValidMove(arrow)
    } else {
      handleBlockedMove(arrow, blocker)
    }
  }

  private fun handleValidMove(arrow: ArrowItem) {
    val state = _uiState.value
    val currentMoves = state.moves + 1
    val newCombo = state.comboStreak + 1

    // Play procedural dynamic audio & haptics
    GameAudioSynthesizer.playSwipeSwoosh(state.soundEnabled)
    GameAudioSynthesizer.playComboChime(newCombo, state.soundEnabled)
    GameAudioSynthesizer.vibrate(30, (140 + newCombo * 20).coerceAtMost(255), state.vibrationEnabled)

    // Generate floating popup text
    val popupText = when {
      newCombo >= 4 -> "ULTRA ESCAPE! 🔥 x$newCombo"
      newCombo >= 2 -> "COMBO x$newCombo! ⚡"
      else -> "+1000"
    }

    val newPopup = FloatingTextPopup(
      id = System.nanoTime(),
      text = popupText,
      x = arrow.col.toFloat(),
      y = arrow.row.toFloat(),
      color = arrow.color.hex,
      isCombo = newCombo >= 2
    )

    // Step 1: Transition to MOVING state and trigger exit animation
    _uiState.update { current ->
      val updatedArrows = current.arrows.map {
        if (it.id == arrow.id) it.copy(isMovingOut = true, exitProgress = 1f, isHighlightedForHint = false)
        else it
      }
      current.copy(
        inputState = InputState.MOVING,
        moves = currentMoves,
        comboStreak = newCombo,
        arrows = updatedArrows,
        highlightedHintArrowId = null,
        particles = current.particles + spawnSparks(arrow, newCombo),
        floatingPopups = (current.floatingPopups + newPopup).takeLast(4)
      )
    }

    // Step 2: After movement animation finishes (~300ms), cleanup and check win
    viewModelScope.launch {
      delay(320)

      var isLevelWon = false
      _uiState.update { current ->
        val updatedArrows = current.arrows.map {
          if (it.id == arrow.id) it.copy(isActive = false, isMovingOut = false)
          else it
        }
        val remainingActive = updatedArrows.count { it.isActive }
        isLevelWon = remainingActive == 0

        current.copy(
          inputState = if (isLevelWon) InputState.FINISHED else InputState.IDLE,
          arrows = updatedArrows
        )
      }

      if (isLevelWon) {
        handleWin()
      }
    }
  }

  private fun handleBlockedMove(arrow: ArrowItem, blocker: ArrowItem?) {
    val state = _uiState.value
    val newLives = state.lives - 1
    val isFailed = newLives <= 0

    // Play collision shock audio & heavy vibration
    GameAudioSynthesizer.playImpactThud(state.soundEnabled)
    GameAudioSynthesizer.vibrate(60, 240, state.vibrationEnabled)

    val collisionPopup = FloatingTextPopup(
      id = System.nanoTime(),
      text = "BLOCKED! 💥",
      x = arrow.col.toFloat(),
      y = arrow.row.toFloat(),
      color = 0xFFF43F5E,
      isCombo = false
    )

    // Trigger shake animation & deduct life, reset combo
    _uiState.update { current ->
      val updatedArrows = current.arrows.map {
        if (it.id == arrow.id) it.copy(isBlockedShaking = true)
        else it
      }
      current.copy(
        inputState = InputState.MOVING,
        lives = newLives,
        comboStreak = 0,
        boardShakeTrigger = System.currentTimeMillis(),
        arrows = updatedArrows,
        floatingPopups = (current.floatingPopups + collisionPopup).takeLast(4)
      )
    }

    viewModelScope.launch {
      delay(400) // Shake duration
      if (isFailed) {
        handleFail()
      } else {
        _uiState.update { current ->
          val clearedShakeArrows = current.arrows.map {
            if (it.id == arrow.id) it.copy(isBlockedShaking = false)
            else it
          }
          current.copy(
            inputState = InputState.IDLE,
            arrows = clearedShakeArrows
          )
        }
      }
    }
  }

  private fun handleWin() {
    timerJob?.cancel()
    val state = _uiState.value
    val level = state.level
    val moves = state.moves
    val time = state.timerSeconds
    val lives = state.lives

    GameAudioSynthesizer.playVictoryFanfare(state.soundEnabled)
    GameAudioSynthesizer.vibrate(80, 255, state.vibrationEnabled)

    val stars = when {
      moves <= level.star3Moves && lives == 3 -> 3
      moves <= level.star2Moves && lives >= 2 -> 2
      else -> 1
    }

    val baseScore = 1000 * stars
    val moveBonus = maxOf(0, (level.maxMoves - moves) * 100)
    val timeBonus = maxOf(0, (level.timerTargetSeconds - time) * 20)
    val lifeBonus = lives * 250
    val totalScore = baseScore + moveBonus + timeBonus + lifeBonus

    val isNewBest = moves < state.bestMovesForLevel || state.bestMovesForLevel == 0

    val result = LevelResult(
      levelId = level.levelId,
      moves = moves,
      maxMoves = level.maxMoves,
      stars = stars,
      timeSeconds = time,
      score = totalScore,
      isNewBest = isNewBest,
      isPerfect = stars == 3
    )

    _uiState.update {
      it.copy(
        gameStatus = GameStatus.WON,
        inputState = InputState.FINISHED,
        result = result
      )
    }

    // Save progress to database
    viewModelScope.launch {
      repository.recordLevelResult(
        levelId = level.levelId,
        starsEarned = stars,
        moves = moves,
        timeSeconds = time,
        score = totalScore,
        totalLevelsCount = LevelCatalog.TOTAL_LEVELS
      )
    }
  }

  private fun handleFail() {
    timerJob?.cancel()
    GameAudioSynthesizer.playFailSound(_uiState.value.soundEnabled)
    _uiState.update {
      it.copy(
        gameStatus = GameStatus.FAILED,
        inputState = InputState.FINISHED
      )
    }
  }

  fun requestHint(activity: Activity? = null) {
    val state = _uiState.value
    if (state.gameStatus != GameStatus.PLAYING || state.inputState != InputState.IDLE) return

    viewModelScope.launch {
      val hasFreeTicket = repository.useHintTicket()
      if (hasFreeTicket) {
        showComputedHint()
      } else {
        // Hint is locked until user watches 1 ad (1 ad = 1 hint)
        showRewardedAdForHint(activity = activity)
      }
    }
  }

  fun requestRewardedAdForTickets(activity: Activity? = null) {
    viewModelScope.launch {
      showRewardedAdForHint(activity = activity)
    }
  }

  private fun showRewardedAdForHint(activity: Activity? = null) {
    _uiState.update {
      it.copy(
        isAdShowing = true,
        adSecondsRemaining = AdConfig.REWARDED_DURATION_SECONDS,
        adSponsor = "Google AdMob"
      )
    }

    adManager.showRewardedAd(
      activity = activity,
      context = getApplication(),
      onRewardEarned = {
        viewModelScope.launch {
          // 1 Ad = 1 Hint unlocked & shown
          showComputedHint()
        }
      },
      onAdDismissed = {
        _uiState.update { it.copy(isAdShowing = false, adMessage = null) }
      },
      onAdFailed = { reason ->
        _uiState.update {
          it.copy(
            isAdShowing = false,
            adMessage = "Ad status: $reason. 1 Hint unlocked!"
          )
        }
        showComputedHint()
      }
    )
  }

  private fun showComputedHint() {
    val state = _uiState.value
    val activeArrows = state.arrows.filter { it.isActive }
    val bestHint = PuzzleSolver.findBestHint(
      activeArrows = activeArrows,
      rows = state.level.rows,
      cols = state.level.cols
    )

    if (bestHint != null) {
      GameAudioSynthesizer.playHintChime(state.soundEnabled)
      _uiState.update { current ->
        val updatedArrows = current.arrows.map {
          if (it.id == bestHint.id) it.copy(isHighlightedForHint = true)
          else it.copy(isHighlightedForHint = false)
        }
        current.copy(
          arrows = updatedArrows,
          highlightedHintArrowId = bestHint.id
        )
      }
    }
  }

  fun dismissAd() {
    adManager.cancelRewarded()
    _uiState.update { it.copy(isAdShowing = false) }
  }

  fun triggerInterstitialAd(activity: Activity? = null) {
    adManager.showInterstitialAd(
      activity = activity,
      context = getApplication(),
      onAdDismissed = {
        _uiState.update { it.copy(isInterstitialShowing = false) }
      }
    )
  }

  fun dismissInterstitialAd() {
    adManager.dismissInterstitial()
    _uiState.update { it.copy(isInterstitialShowing = false) }
  }

  fun rotateBannerCreative() {
    adManager.rotateBannerCreative()
  }

  fun toggleBanner(visible: Boolean) {
    adManager.toggleBanner(visible)
  }

  fun toggleSettings(sound: Boolean, vibration: Boolean, colorblind: Boolean) {
    viewModelScope.launch {
      repository.updateSettings(sound, vibration, colorblind)
    }
  }

  private fun spawnSparks(arrow: ArrowItem, combo: Int = 1): List<SparkParticle> {
    val count = (16 + combo * 4).coerceAtMost(36)
    val sparks = mutableListOf<SparkParticle>()
    for (i in 0 until count) {
      val angle = (random.nextFloat() * Math.PI * 2).toFloat()
      val speed = (random.nextFloat() * 18f + 6f)
      sparks.add(
        SparkParticle(
          id = System.nanoTime() + i,
          x = arrow.col.toFloat(),
          y = arrow.row.toFloat(),
          vx = (Math.cos(angle.toDouble()) * speed).toFloat(),
          vy = (Math.sin(angle.toDouble()) * speed).toFloat(),
          color = arrow.color.hex,
          size = random.nextFloat() * 10f + 4f
        )
      )
    }
    return sparks
  }
}

