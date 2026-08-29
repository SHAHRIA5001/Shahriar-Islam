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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TestInterstitialDialog(
  secondsRemaining: Int,
  sponsorName: String,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("test_interstitial_dialog"),
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
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFEF3C7)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TEST INTERSTITIAL AD",
                color = Color(0xFFB45309),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(BentoContainerDeep)
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close Ad",
              tint = TextPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Ad Creative Box
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
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
                imageVector = Icons.Filled.RocketLaunch,
                contentDescription = null,
                tint = BentoPrimary,
                modifier = Modifier.size(44.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Next-Gen Puzzle Experience",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center
              )
              Text(
                text = "AdMob Interstitial Unit ID:\n${AdConfig.INTERSTITIAL_ID}",
                fontSize = 10.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons
        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("interstitial_continue_btn"),
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
        ) {
          Text(
            text = if (secondsRemaining > 0) "CONTINUE GAME (${secondsRemaining}s)" else "CONTINUE GAME",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = Color.White
          )
        }
      }
    }
  }
}
