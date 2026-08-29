package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.LevelResult
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun WinDialog(
  result: LevelResult,
  hasNextLevel: Boolean,
  onNextLevel: () -> Unit,
  onReplay: () -> Unit,
  onMapClick: () -> Unit
) {
  val star1Scale = remember { Animatable(0f) }
  val star2Scale = remember { Animatable(0f) }
  val star3Scale = remember { Animatable(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "win_aura")
  val rotationDeg by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(8000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "win_rotation"
  )

  LaunchedEffect(result.stars) {
    if (result.stars >= 1) {
      delay(150)
      star1Scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    if (result.stars >= 2) {
      delay(200)
      star2Scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    if (result.stars >= 3) {
      delay(200)
      star3Scale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
  }

  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("win_dialog"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = BentoSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // High-res 3D Cyber Trophy art with radiant rotating halo
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(100.dp)
        ) {
          // Radiant aura
          Box(
            modifier = Modifier
              .size(96.dp)
              .rotate(rotationDeg)
              .clip(CircleShape)
              .background(
                Brush.sweepGradient(
                  listOf(
                    AmberGold.copy(alpha = 0.6f),
                    BentoPrimary.copy(alpha = 0.5f),
                    Color(0xFF10B981).copy(alpha = 0.6f),
                    AmberGold.copy(alpha = 0.6f)
                  )
                )
              )
          )

          Image(
            painter = painterResource(id = R.drawable.img_victory_trophy_art_1787935573355),
            contentDescription = "Victory Trophy",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(84.dp)
              .clip(CircleShape)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "LEVEL ${result.levelId}",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp,
          color = BentoPrimary
        )

        Text(
          text = if (result.isPerfect) "PERFECT ESCAPE!" else "VICTORY!",
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 0.5.sp,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Animated 3 Stars
        Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Star 1",
            tint = if (result.stars >= 1) AmberGold else BentoDivider,
            modifier = Modifier
              .size(36.dp)
              .scale(star1Scale.value)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Star 2",
            tint = if (result.stars >= 2) AmberGold else BentoDivider,
            modifier = Modifier
              .size(46.dp)
              .scale(star2Scale.value)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Star 3",
            tint = if (result.stars >= 3) AmberGold else BentoDivider,
            modifier = Modifier
              .size(36.dp)
              .scale(star3Scale.value)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bento Stat Grid
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          color = BentoContainerDeep
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Moves
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "MOVES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
              )
              Text(
                text = "${result.moves}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
              )
            }

            // Divider
            Box(
              modifier = Modifier
                .width(1.dp)
                .height(30.dp)
                .background(BentoDivider)
            )

            // Time
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "TIME",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
              )
              val minutes = result.timeSeconds / 60
              val seconds = result.timeSeconds % 60
              Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
              )
            }

            // Divider
            Box(
              modifier = Modifier
                .width(1.dp)
                .height(30.dp)
                .background(BentoDivider)
            )

            // Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "SCORE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
              )
              Text(
                text = "${result.score}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = BentoPrimary
              )
            }
          }
        }

        if (result.isNewBest) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFD1FAE5),
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            Text(
              text = "★ NEW BEST RECORD ★",
              color = Emerald500,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        if (hasNextLevel) {
          Button(
            onClick = onNextLevel,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("next_level_button"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
          ) {
            Text(
              text = "NEXT LEVEL",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onReplay,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("win_replay_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = BentoContainerDeep,
              contentColor = TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoDivider)
          ) {
            Icon(
              imageVector = Icons.Filled.Refresh,
              contentDescription = "Replay",
              tint = TextSecondary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "REPLAY",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = TextPrimary
            )
          }

          OutlinedButton(
            onClick = onMapClick,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("win_map_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = BentoContainerDeep,
              contentColor = TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoDivider)
          ) {
            Icon(
              imageVector = Icons.Filled.Map,
              contentDescription = "Mission Map",
              tint = TextSecondary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "MAP",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = TextPrimary
            )
          }
        }
      }
    }
  }
}

