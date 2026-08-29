package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ads.AdConfig
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RewardedAdDialog(
  secondsRemaining: Int,
  sponsorName: String,
  onDismiss: () -> Unit
) {
  val totalSec = AdConfig.REWARDED_DURATION_SECONDS.toFloat()
  val progress = ((totalSec - secondsRemaining) / totalSec).coerceIn(0f, 1f)

  Dialog(
    onDismissRequest = { /* Prevent dismiss before complete */ },
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("rewarded_ad_dialog"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = BentoSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Ad Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = BentoContainerDeep
          ) {
            Text(
              text = "SPONSORED REWARD",
              color = BentoPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          Surface(
            shape = CircleShape,
            color = BentoContainerDeep
          ) {
            Text(
              text = "${secondsRemaining}s",
              color = TextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bento Ad Visual Display
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
          shape = RoundedCornerShape(20.dp),
          color = BentoContainerDeep
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(16.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = null,
                tint = BentoPrimary,
                modifier = Modifier.size(44.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = sponsorName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center
              )
              Text(
                text = "Official Partner • High Speed Puzzle Engine",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = BentoPrimary,
          trackColor = BentoDivider.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = AmberGold,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (secondsRemaining > 0) "Reward unlocking in ${secondsRemaining}s..." else "Reward Unlocked!",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (secondsRemaining == 0) Emerald500 else TextSecondary
          )
        }
      }
    }
  }
}

