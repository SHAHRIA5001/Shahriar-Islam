package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import com.example.model.LevelDefinition
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.Rose500
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GameHeader(
  level: LevelDefinition,
  lives: Int,
  startingLives: Int,
  timerSeconds: Int,
  onBackClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left: Back button + Title & Chapter Subtitle
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(BentoSurface)
          .testTag("back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back to Mission Map",
          tint = TextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Text(
          text = "MISSION MAP",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = BentoPrimary.copy(alpha = 0.8f)
        )
        Text(
          text = "Level ${level.levelId}",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.5).sp,
          color = TextPrimary
        )
      }
    }

    // Right: Bento Pill with Lives + Vertical Divider + Monospaced Timer & Settings
    Row(verticalAlignment = Alignment.CenterVertically) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = BentoSurface,
        modifier = Modifier.padding(end = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Lives Hearts
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            for (i in 1..startingLives) {
              val hasLife = i <= lives
              val heartScale by animateFloatAsState(
                targetValue = if (hasLife) 1f else 0.8f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "heart_scale"
              )

              Icon(
                imageVector = if (hasLife) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Life $i",
                tint = if (hasLife) Rose500 else BentoDivider,
                modifier = Modifier
                  .size(18.dp)
                  .scale(heartScale)
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Divider
          Box(
            modifier = Modifier
              .width(1.dp)
              .height(16.dp)
              .background(BentoDivider)
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Monospaced Timer
          val minutes = timerSeconds / 60
          val seconds = timerSeconds % 60
          val formattedTime = "%02d:%02d".format(minutes, seconds)
          Text(
            text = formattedTime,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
          )
        }
      }

      IconButton(
        onClick = onSettingsClick,
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(BentoSurface)
          .testTag("settings_button")
      ) {
        Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = "Settings",
          tint = TextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun DifficultyBadge(difficulty: Difficulty) {
  val (label, bgColor, textColor) = when (difficulty) {
    Difficulty.EASY -> Triple("EASY", Color(0xFFD1FAE5), Color(0xFF065F46))
    Difficulty.MEDIUM -> Triple("MEDIUM", Color(0xFFDBEAFE), Color(0xFF1E40AF))
    Difficulty.HARD -> Triple("HARD", Color(0xFFFEF3C7), Color(0xFF92400E))
    Difficulty.EXPERT -> Triple("EXPERT", Color(0xFFFEE2E2), Color(0xFF991B1B))
    Difficulty.MASTER -> Triple("MASTER", Color(0xFFEDE9FE), Color(0xFF5B21B6))
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bgColor,
    modifier = Modifier.padding(top = 2.dp)
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 10.sp,
      fontWeight = FontWeight.ExtraBold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
    )
  }
}

@Composable
fun GameStatusBar(
  moves: Int,
  maxMoves: Int,
  targetMoves: Int,
  difficulty: Difficulty,
  modifier: Modifier = Modifier
) {
  // Bento Grid Moves Card
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = BentoContainerDeep),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Target Moves
      Column {
        Text(
          text = "Target Moves",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "$targetMoves",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          DifficultyBadge(difficulty = difficulty)
        }
      }

      // Right: Current / Max
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "Current",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = TextSecondary
        )
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = "$moves",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = BentoPrimary
          )
          Text(
            text = " / $maxMoves",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
          )
        }
      }
    }
  }
}

@Composable
fun GameActionBar(
  hintTickets: Int,
  isInteractive: Boolean,
  onHintClick: () -> Unit,
  onResetClick: () -> Unit,
  onMapClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // 1. Reset Button in Bento Surface
    Surface(
      modifier = Modifier
        .weight(1f)
        .height(60.dp)
        .clip(RoundedCornerShape(24.dp))
        .clickable(enabled = isInteractive) { onResetClick() }
        .testTag("reset_button"),
      shape = RoundedCornerShape(24.dp),
      color = BentoSurface
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Filled.Refresh,
          contentDescription = "Reset",
          tint = TextSecondary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "RESET",
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          fontSize = 11.sp,
          color = TextSecondary
        )
      }
    }

    // 2. Hint Button in Bento Primary with badge
    Surface(
      modifier = Modifier
        .weight(1.5f)
        .height(60.dp)
        .clip(RoundedCornerShape(24.dp))
        .clickable(enabled = isInteractive) { onHintClick() }
        .testTag("hint_button"),
      shape = RoundedCornerShape(24.dp),
      color = if (isInteractive) BentoPrimary else BentoPrimary.copy(alpha = 0.5f),
      shadowElevation = 4.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Filled.AutoAwesome,
          contentDescription = "Hint",
          tint = AmberGold,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "HINT",
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            fontSize = 11.sp,
            color = Color.White
          )
          Text(
            text = if (hintTickets > 0) "TICKETS: $hintTickets" else "REWARD AD",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.8f)
          )
        }
      }
    }

    // 3. Mission Map Quick Jump Button
    Surface(
      modifier = Modifier
        .weight(1f)
        .height(60.dp)
        .clip(RoundedCornerShape(24.dp))
        .clickable(enabled = isInteractive) { onMapClick() }
        .testTag("map_nav_button"),
      shape = RoundedCornerShape(24.dp),
      color = BentoSurface
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Filled.Map,
          contentDescription = "Mission Map",
          tint = TextSecondary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "MAP",
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          fontSize = 11.sp,
          color = TextSecondary
        )
      }
    }
  }
}

