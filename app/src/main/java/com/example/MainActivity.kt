package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MissionMapScreen
import com.example.ui.theme.ArrowEscapeTheme
import com.example.ui.theme.BentoBackground
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.LevelSelectViewModel

enum class AppScreen {
  MISSION_MAP,
  GAMEPLAY
}

class MainActivity : ComponentActivity() {

  private val gameViewModel: GameViewModel by viewModels()
  private val levelSelectViewModel: LevelSelectViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ArrowEscapeTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = BentoBackground
        ) {
          ArrowEscapeApp(
            gameViewModel = gameViewModel,
            levelSelectViewModel = levelSelectViewModel
          )
        }
      }
    }
  }
}

@Composable
fun ArrowEscapeApp(
  gameViewModel: GameViewModel,
  levelSelectViewModel: LevelSelectViewModel
) {
  var currentScreen by remember { mutableStateOf(AppScreen.MISSION_MAP) }
  val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()
  val levelSelectUiState by levelSelectViewModel.uiState.collectAsStateWithLifecycle()
  val userPrefs by gameViewModel.userPreferences.collectAsStateWithLifecycle()

  BackHandler(enabled = currentScreen == AppScreen.GAMEPLAY) {
    currentScreen = AppScreen.MISSION_MAP
  }

  AnimatedContent(
    targetState = currentScreen,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "screen_transition"
  ) { screen ->
    when (screen) {
      AppScreen.MISSION_MAP -> {
        MissionMapScreen(
          uiState = levelSelectUiState,
          soundEnabled = userPrefs.soundEnabled,
          vibrationEnabled = userPrefs.vibrationEnabled,
          colorblindMode = userPrefs.colorblindMode,
          onSelectLevel = { levelId ->
            gameViewModel.loadLevel(levelId)
            currentScreen = AppScreen.GAMEPLAY
          },
          onSelectWorld = { worldIdx ->
            levelSelectViewModel.selectWorld(worldIdx)
          },
          onClaimDailyReward = {
            levelSelectViewModel.claimDailyHintReward()
          },
          onToggleSettings = { sound, vibration, colorblind ->
            gameViewModel.toggleSettings(sound, vibration, colorblind)
          }
        )
      }
      AppScreen.GAMEPLAY -> {
        GameScreen(
          uiState = gameUiState,
          viewModel = gameViewModel,
          onBackToMap = {
            currentScreen = AppScreen.MISSION_MAP
          }
        )
      }
    }
  }
}

