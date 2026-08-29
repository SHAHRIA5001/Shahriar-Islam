package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
  soundEnabled: Boolean,
  vibrationEnabled: Boolean,
  colorblindMode: Boolean,
  onToggleSettings: (Boolean, Boolean, Boolean) -> Unit,
  onOpenTestAdsInspector: (() -> Unit)? = null,
  onDismiss: () -> Unit
) {
  var currentSound by remember { mutableStateOf(soundEnabled) }
  var currentVibration by remember { mutableStateOf(vibrationEnabled) }
  var currentColorblind by remember { mutableStateOf(colorblindMode) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("settings_dialog"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = BentoSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Settings,
              contentDescription = null,
              tint = BentoPrimary,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "SETTINGS",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.2.sp,
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

        Spacer(modifier = Modifier.height(18.dp))

        // Sound FX Toggle Bento Row
        SettingsToggleRow(
          title = "Sound Effects",
          subtitle = "Dynamic synthesizers and audio feedback",
          checked = currentSound,
          onCheckedChange = {
            currentSound = it
            onToggleSettings(currentSound, currentVibration, currentColorblind)
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Vibration Toggle Bento Row
        SettingsToggleRow(
          title = "Haptic Vibration",
          subtitle = "Tactile response on collisions & victory",
          checked = currentVibration,
          onCheckedChange = {
            currentVibration = it
            onToggleSettings(currentSound, currentVibration, currentColorblind)
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Colorblind Mode Toggle Bento Row
        SettingsToggleRow(
          title = "Colorblind Mode",
          subtitle = "Displays high-contrast direction symbols",
          checked = currentColorblind,
          onCheckedChange = {
            currentColorblind = it
            onToggleSettings(currentSound, currentVibration, currentColorblind)
          }
        )

        if (onOpenTestAdsInspector != null) {
          Spacer(modifier = Modifier.height(12.dp))

          // Test Ads Suite Button
          OutlinedButton(
            onClick = {
              onDismiss()
              onOpenTestAdsInspector()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("settings_test_ads_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = BentoContainerDeep,
              contentColor = TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoDivider)
          ) {
            Icon(
              imageVector = Icons.Filled.BugReport,
              contentDescription = "Test Ads Suite",
              tint = BentoPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "TEST ADS SUITE (ADMOB)",
              fontWeight = FontWeight.Black,
              fontSize = 12.sp,
              letterSpacing = 0.5.sp,
              color = BentoPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Done Button
        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("settings_done_button"),
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
        ) {
          Text(
            text = "DONE",
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            fontSize = 14.sp,
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
private fun SettingsToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = BentoContainerDeep,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = TextSecondary,
          lineHeight = 14.sp
        )
      }

      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = BentoPrimary,
          uncheckedThumbColor = TextMuted,
          uncheckedTrackColor = BentoSurface
        )
      )
    }
  }
}
