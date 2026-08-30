package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.engine.LevelCatalog
import com.example.model.GameStatus
import com.example.model.InputState
import com.example.ui.components.FailDialog
import com.example.ui.components.GameActionBar
import com.example.ui.components.GameBoard
import com.example.ui.components.GameHeader
import com.example.ui.components.GameStatusBar
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TestAdsInspectorDialog
import com.example.ui.components.TestBannerAd
import com.example.ui.components.TestInterstitialDialog
import com.example.ui.components.WinDialog
import com.example.ui.theme.BentoBackground
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun GameScreen(
  uiState: GameUiState,
  viewModel: GameViewModel,
  onBackToMap: () -> Unit
) {
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showTestAdsInspector by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val bannerCreative by viewModel.bannerCreative.collectAsState()
  val context = LocalContext.current
  val activity = context as? Activity

  LaunchedEffect(uiState.adMessage) {
    uiState.adMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  val isInteractive = uiState.inputState == InputState.IDLE && uiState.gameStatus == GameStatus.PLAYING && !uiState.isAdShowing && !uiState.isInterstitialShowing

  Scaffold(
    containerColor = BentoBackground,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = Modifier
      .fillMaxSize()
      .safeDrawingPadding()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Bento Top Header (Mission map label, Level title, Lives & Timer Pill, Settings)
      GameHeader(
        level = uiState.level,
        lives = uiState.lives,
        startingLives = uiState.level.startingLives,
        timerSeconds = uiState.timerSeconds,
        onBackClick = onBackToMap,
        onSettingsClick = { showSettingsDialog = true }
      )

      // 2. Bento Stats Card (Target Moves, Difficulty Badge, Current / Max Moves)
      GameStatusBar(
        moves = uiState.moves,
        maxMoves = uiState.level.maxMoves,
        targetMoves = uiState.level.targetMoves,
        difficulty = uiState.level.difficulty,
        modifier = Modifier.padding(top = 2.dp)
      )

      // 3. Bento Interactive Puzzle Grid
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        GameBoard(
          rows = uiState.level.rows,
          cols = uiState.level.cols,
          arrows = uiState.arrows,
          isInteractive = isInteractive,
          colorblindMode = uiState.colorblindMode,
          particles = uiState.particles,
          floatingPopups = uiState.floatingPopups,
          shakeTrigger = uiState.boardShakeTrigger,
          onArrowClick = { arrowId -> viewModel.onArrowClicked(arrowId) }
        )
      }

      // 4. Bento Bottom Controls (Reset, Hint, Map)
      GameActionBar(
        hintTickets = uiState.hintTickets,
        isInteractive = isInteractive,
        onHintClick = { viewModel.requestHint(activity) },
        onResetClick = { viewModel.resetLevel() },
        onMapClick = onBackToMap
      )

      // 5. Test Banner Ad at bottom
      if (uiState.bannerVisible) {
        TestBannerAd(
          creative = bannerCreative,
          onRotateCreative = { viewModel.rotateBannerCreative() },
          onDismiss = { viewModel.toggleBanner(false) },
          modifier = Modifier.padding(bottom = 6.dp)
        )
      }
    }
  }

  // Win Dialog Overlay
  if (uiState.gameStatus == GameStatus.WON && uiState.result != null) {
    WinDialog(
      result = uiState.result,
      hasNextLevel = uiState.level.levelId < LevelCatalog.TOTAL_LEVELS,
      onNextLevel = {
        // Trigger Interstitial Ad test every 3 levels or on demand
        if (uiState.level.levelId % 3 == 0) {
          viewModel.triggerInterstitialAd(activity)
        }
        viewModel.nextLevel()
      },
      onReplay = { viewModel.resetLevel() },
      onMapClick = onBackToMap
    )
  }

  // Fail Dialog Overlay
  if (uiState.gameStatus == GameStatus.FAILED) {
    FailDialog(
      levelId = uiState.level.levelId,
      onRetry = { viewModel.resetLevel() },
      onHintAd = {
        viewModel.resetLevel()
        viewModel.requestRewardedAdForTickets(activity)
      },
      onMapClick = onBackToMap
    )
  }

  // Rewarded Ad Dialog Overlay
  if (uiState.isAdShowing) {
    RewardedAdDialog(
      secondsRemaining = uiState.adSecondsRemaining,
      sponsorName = uiState.adSponsor,
      onDismiss = { viewModel.dismissAd() }
    )
  }

  // Interstitial Ad Dialog Overlay
  if (uiState.isInterstitialShowing) {
    TestInterstitialDialog(
      secondsRemaining = uiState.adSecondsRemaining,
      sponsorName = uiState.adSponsor,
      onDismiss = { viewModel.dismissInterstitialAd() }
    )
  }

  // Settings Dialog Overlay
  if (showSettingsDialog) {
    SettingsDialog(
      soundEnabled = uiState.soundEnabled,
      vibrationEnabled = uiState.vibrationEnabled,
      colorblindMode = uiState.colorblindMode,
      onToggleSettings = { sound, vibration, colorblind ->
        viewModel.toggleSettings(sound, vibration, colorblind)
      },
      onOpenTestAdsInspector = {
        showTestAdsInspector = true
      },
      onDismiss = { showSettingsDialog = false }
    )
  }

  // Test Ads Inspector Dialog
  if (showTestAdsInspector) {
    TestAdsInspectorDialog(
      onTriggerRewarded = {
        showTestAdsInspector = false
        viewModel.requestRewardedAdForTickets()
      },
      onTriggerInterstitial = {
        showTestAdsInspector = false
        viewModel.triggerInterstitialAd()
      },
      onDismiss = { showTestAdsInspector = false }
    )
  }
}
