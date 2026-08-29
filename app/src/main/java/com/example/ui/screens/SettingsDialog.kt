package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryNeon

@Composable
fun SettingsDialog(
  soundEnabled: Boolean,
  vibrationEnabled: Boolean,
  colorblindMode: Boolean,
  onToggleSettings: (Boolean, Boolean, Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("settings_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131C30)),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SETTINGS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color.White
          )
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("settings_close_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close",
              tint = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound Toggle
        SettingSwitchRow(
          icon = Icons.Filled.MusicNote,
          title = "Sound Effects",
          description = "Audio feedback on tap and exit",
          checked = soundEnabled,
          onCheckedChange = { onToggleSettings(it, vibrationEnabled, colorblindMode) }
        )

        HorizontalDivider(
          modifier = Modifier.padding(vertical = 8.dp),
          color = Color(0xFF1E293B)
        )

        // Haptics Toggle
        SettingSwitchRow(
          icon = Icons.Filled.Vibration,
          title = "Haptic Vibration",
          description = "Vibrate on blocked arrow collision",
          checked = vibrationEnabled,
          onCheckedChange = { onToggleSettings(soundEnabled, it, colorblindMode) }
        )

        HorizontalDivider(
          modifier = Modifier.padding(vertical = 8.dp),
          color = Color(0xFF1E293B)
        )

        // Colorblind Mode Toggle
        SettingSwitchRow(
          icon = Icons.Filled.Accessibility,
          title = "Colorblind Mode",
          description = "Show directional symbols on arrow tiles",
          checked = colorblindMode,
          onCheckedChange = { onToggleSettings(soundEnabled, vibrationEnabled, it) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // How to play section
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF1E2942),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.HelpOutline,
                contentDescription = null,
                tint = SecondaryNeon,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "HOW TO PLAY",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryNeon
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "• Tap any arrow to launch it in its pointing direction.\n" +
                "• If unobstructed to the board edge, it escapes safely!\n" +
                "• If blocked by another arrow, you lose 1 heart.\n" +
                "• Clear all arrows to finish the level and unlock the next!",
              fontSize = 12.sp,
              lineHeight = 18.sp,
              color = Color(0xFFCBD5E1)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
        ) {
          Text(text = "DONE", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun SettingSwitchRow(
  icon: ImageVector,
  title: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = SecondaryNeon,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color.White
        )
        Text(
          text = description,
          fontSize = 11.sp,
          color = Color(0xFF94A3B8)
        )
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = PrimaryNeon,
        uncheckedThumbColor = Color(0xFF94A3B8),
        uncheckedTrackColor = Color(0xFF1E293B)
      )
    )
  }
}
