package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdConfig
import com.example.ads.BannerCreative
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TestBannerAd(
  creative: BannerCreative,
  onRotateCreative: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .testTag("test_banner_ad"),
    shape = RoundedCornerShape(16.dp),
    color = BentoSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, BentoDivider),
    shadowElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Test Ad Badge & Creative Icon
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BentoContainerDeep),
          contentAlignment = Alignment.Center
        ) {
          Text(text = creative.iconEmoji, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFFEF3C7) // Amber tint for Test Ad tag
            ) {
              Text(
                text = "TEST AD",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFB45309),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = creative.advertiser,
              fontSize = 10.sp,
              color = TextMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Text(
            text = creative.headline,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Actions: CTA & Refresh/Close
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Button(
          onClick = onRotateCreative,
          modifier = Modifier
            .height(30.dp)
            .testTag("banner_ad_cta"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
        ) {
          Text(
            text = creative.callToAction,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Hide Banner",
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
