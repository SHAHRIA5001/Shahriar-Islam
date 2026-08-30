package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.db.GameDatabase
import com.example.data.db.GameRepository
import com.example.data.db.LevelProgressEntity
import com.example.data.db.UserPreferencesEntity
import com.example.engine.LevelCatalog
import com.example.model.Difficulty
import com.example.model.LevelDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorldChapter(
  val id: Int,
  val name: String,
  val subtitle: String,
  val levelRange: IntRange,
  val difficulty: Difficulty,
  val iconEmoji: String,
  val bannerDrawableRes: Int
)

data class LevelItemUi(
  val levelId: Int,
  val name: String,
  val isUnlocked: Boolean,
  val isCompleted: Boolean,
  val isCurrent: Boolean,
  val stars: Int,
  val bestMoves: Int,
  val bestTimeSeconds: Int,
  val difficulty: Difficulty,
  val rows: Int,
  val cols: Int,
  val arrowCount: Int
)

data class LevelSelectUiState(
  val selectedWorldIndex: Int = 0,
  val worlds: List<WorldChapter> = emptyList(),
  val levels: List<LevelItemUi> = emptyList(),
  val totalStars: Int = 0,
  val totalMaxStars: Int = LevelCatalog.TOTAL_LEVELS * 3,
  val completedLevelsCount: Int = 0,
  val currentUnlockedLevelId: Int = 1,
  val hintTickets: Int = 0
)

class LevelSelectViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: GameRepository

  private val _selectedWorld = MutableStateFlow(0)
  val selectedWorld: StateFlow<Int> = _selectedWorld.asStateFlow()

  val chapters = listOf(
    WorldChapter(0, "Genesis", "Learn the Flow (3x3 - 4x4)", 1..30, Difficulty.EASY, "🌱", R.drawable.img_cyber_city_banner_1787935510004),
    WorldChapter(1, "Crossroads", "First Intersections (4x4 - 5x5)", 31..65, Difficulty.MEDIUM, "⚡", R.drawable.img_synthwave_sunset_world_1787935554541),
    WorldChapter(2, "Labyrinth", "Tangled Paths (5x5 - 6x6)", 66..105, Difficulty.MEDIUM, "🌀", R.drawable.img_cosmic_nebula_world_1787935526741),
    WorldChapter(3, "Matrix", "Dense Directional Grid (6x6)", 106..145, Difficulty.HARD, "🔮", R.drawable.img_quantum_core_world_1787935541637),
    WorldChapter(4, "Citadel", "Complex Weaves & Locks (7x6)", 146..185, Difficulty.EXPERT, "🏰", R.drawable.img_cyber_city_banner_1787935510004),
    WorldChapter(5, "Grandmaster", "The Ultimate Escapes (7x7 - 8x8)", 186..220, Difficulty.MASTER, "👑", R.drawable.img_victory_trophy_art_1787935573355)
  )

  val uiState: StateFlow<LevelSelectUiState>

  init {
    val database = GameDatabase.getDatabase(application)
    repository = GameRepository(database.levelProgressDao())

    uiState = combine(
      repository.allProgress,
      repository.userPreferences,
      _selectedWorld
    ) { progressList, prefs, worldIdx ->
      val progressMap = progressList.associateBy { it.levelId }
      val currentMaxUnlocked = maxOf(prefs.currentUnlockedLevel, 1)

      val allCatalogLevels = LevelCatalog.getAllLevels()
      val selectedChapter = chapters[worldIdx]
      val chapterLevels = allCatalogLevels.filter { it.levelId in selectedChapter.levelRange }

      val levelItems = chapterLevels.map { levelDef ->
        val progress = progressMap[levelDef.levelId]
        val isUnlocked = levelDef.levelId <= currentMaxUnlocked || (progress?.isUnlocked == true)
        val isCompleted = progress?.isCompleted == true
        val isCurrent = levelDef.levelId == currentMaxUnlocked && !isCompleted

        LevelItemUi(
          levelId = levelDef.levelId,
          name = levelDef.name,
          isUnlocked = isUnlocked,
          isCompleted = isCompleted,
          isCurrent = isCurrent,
          stars = progress?.starsEarned ?: 0,
          bestMoves = progress?.bestMoves ?: 0,
          bestTimeSeconds = progress?.bestTimeSeconds ?: 0,
          difficulty = levelDef.difficulty,
          rows = levelDef.rows,
          cols = levelDef.cols,
          arrowCount = levelDef.arrows.size
        )
      }

      val totalStarsEarned = progressList.sumOf { it.starsEarned }
      val totalCompleted = progressList.count { it.isCompleted }

      LevelSelectUiState(
        selectedWorldIndex = worldIdx,
        worlds = chapters,
        levels = levelItems,
        totalStars = totalStarsEarned,
        totalMaxStars = LevelCatalog.TOTAL_LEVELS * 3,
        completedLevelsCount = totalCompleted,
        currentUnlockedLevelId = currentMaxUnlocked,
        hintTickets = prefs.hintTickets
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = LevelSelectUiState(worlds = chapters)
    )
  }

  fun selectWorld(index: Int) {
    _selectedWorld.value = index.coerceIn(0, chapters.lastIndex)
  }

  fun jumpToCurrentLevelWorld() {
    val currentUnlocked = uiState.value.currentUnlockedLevelId
    val worldIndex = chapters.indexOfFirst { currentUnlocked in it.levelRange }
    if (worldIndex >= 0) {
      selectWorld(worldIndex)
    }
  }

  fun claimDailyHintReward() {
    viewModelScope.launch {
      repository.addHintTicket(3)
    }
  }
}
