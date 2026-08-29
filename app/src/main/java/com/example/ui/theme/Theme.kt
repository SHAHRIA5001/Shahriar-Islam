package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BentoColorScheme = lightColorScheme(
  primary = BentoPrimary,
  onPrimary = Color.White,
  primaryContainer = BentoPrimaryContainer,
  onPrimaryContainer = BentoPrimaryDark,
  secondary = BentoPrimary,
  onSecondary = Color.White,
  secondaryContainer = BentoSurfaceVariant,
  onSecondaryContainer = TextPrimary,
  tertiary = Emerald500,
  onTertiary = Color.White,
  background = BentoBackground,
  onBackground = TextPrimary,
  surface = BentoSurface,
  onSurface = TextPrimary,
  surfaceVariant = BentoContainerDeep,
  onSurfaceVariant = TextSecondary,
  outline = BentoDivider,
  error = Rose500,
  onError = Color.White
)

@Composable
fun ArrowEscapeTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = BentoColorScheme,
    typography = Typography,
    content = content
  )
}


