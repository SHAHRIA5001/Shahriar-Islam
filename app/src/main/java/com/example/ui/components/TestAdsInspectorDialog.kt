package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ads.AdConfig
import com.example.ads.TestAdLog
import com.example.ads.TestAdManager
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TestAdsInspectorDialog(
  adManager: TestAdManager = TestAdManager.instance,
  onTriggerRewarded: () -> Unit,
  onTriggerInterstitial: () -> Unit,
  onDismiss: () -> Unit
) {
  val bannerVisible by adManager.bannerVisible.collectAsState()
  val adLogs by adManager.adLogs.collectAsState()

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .testTag("test_ads_inspector_dialog"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = BentoSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.BugReport,
              contentDescription = null,
              tint = BentoPrimary,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "TEST ADS SUITE",
              fontSize = 17.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              color = TextPrimary,
              modifier = Modifier.padding(start = 8.dp)
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close",
              tint = TextSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ad Format Controls & Ad IDs
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Banner Toggle Row
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = BentoContainerDeep,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Banner Ad (320x50)",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "ID: ${AdConfig.BANNER_ID}",
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  color = TextSecondary,
                  maxLines = 1
                )
              }
              Switch(
                checked = bannerVisible,
                onCheckedChange = { adManager.toggleBanner(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = BentoPrimary
                )
              )
            }
          }

          // Trigger Interstitial Button
          Button(
            onClick = onTriggerInterstitial,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("test_trigger_interstitial_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BentoContainerDeep)
          ) {
            Icon(
              imageVector = Icons.Filled.ViewCarousel,
              contentDescription = null,
              tint = BentoPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.Start) {
              Text(
                text = "Interstitial Ad",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }

          // Trigger Rewarded Video Button
          Button(
            onClick = onTriggerRewarded,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("test_trigger_rewarded_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
          ) {
            Icon(
              imageVector = Icons.Filled.AutoAwesome,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Rewarded Video (+3 Hints)",
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 12.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AdMob Configured Units Summary
        Text(
          text = "CONFIGURED ADMOB IDS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.sp,
          color = TextSecondary,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = BentoContainerDeep,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            AdIdRow("App ID", AdConfig.APP_ID)
            AdIdRow("Banner", AdConfig.BANNER_ID)
            AdIdRow("Interstitial", AdConfig.INTERSTITIAL_ID)
            AdIdRow("Rewarded", AdConfig.REWARDED_ID)
            AdIdRow("Rewarded Interstitial", AdConfig.REWARDED_INTERSTITIAL_ID)
            AdIdRow("Native Advanced", AdConfig.NATIVE_ADVANCED_ID)
            AdIdRow("App Open", AdConfig.APP_OPEN_ID)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ad Events Live Telemetry Log
        Text(
          text = "LIVE AD TELEMETRY LOGS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.sp,
          color = TextSecondary,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = BentoContainerDeep,
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
        ) {
          LazyColumn(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            items(adLogs) { log ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "[${log.timestamp}]",
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  color = TextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = when (log.adFormat) {
                    "Rewarded" -> Emerald500.copy(alpha = 0.2f)
                    "Interstitial" -> BentoPrimary.copy(alpha = 0.2f)
                    else -> Color(0xFFFDE68A)
                  }
                ) {
                  Text(
                    text = log.adFormat.take(8),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (log.adFormat) {
                      "Rewarded" -> Emerald500
                      "Interstitial" -> BentoPrimary
                      else -> Color(0xFFB45309)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = log.event,
                  fontSize = 10.sp,
                  color = TextPrimary,
                  fontFamily = FontFamily.Monospace,
                  maxLines = 1
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("inspector_close_btn"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
        ) {
          Text(
            text = "CLOSE INSPECTOR",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
private fun AdIdRow(label: String, id: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary
    )
    Text(
      text = id,
      fontSize = 9.sp,
      fontFamily = FontFamily.Monospace,
      color = TextSecondary,
      maxLines = 1
    )
  }
}
