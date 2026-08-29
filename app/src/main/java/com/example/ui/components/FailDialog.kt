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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.DangerRose
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FailDialog(
  levelId: Int,
  onRetry: () -> Unit,
  onHintAd: () -> Unit,
  onMapClick: () -> Unit
) {
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("fail_dialog"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = BentoSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icon
        Box(
          modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFE4E6)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.HeartBroken,
            contentDescription = "Failed",
            tint = DangerRose,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "BLOCKED!",
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 0.5.sp,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "No more lives remaining. Inspect the path before releasing arrows, or use a hint to find the next clear exit.",
          fontSize = 13.sp,
          color = TextSecondary,
          textAlign = TextAlign.Center,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Retry button
        Button(
          onClick = onRetry,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("retry_level_button"),
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(containerColor = DangerRose),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "RETRY LEVEL",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hint via Rewarded Ad
        Button(
          onClick = onHintAd,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("fail_hint_ad_button"),
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "RETRY WITH FREE HINT",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onMapClick,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("fail_map_button"),
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
            text = "MISSION MAP",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = TextPrimary
          )
        }
      }
    }
  }
}

