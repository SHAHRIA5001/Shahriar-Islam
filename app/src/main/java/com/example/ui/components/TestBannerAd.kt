package com.example.ui.components

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdConfig
import com.example.ads.BannerCreative
import com.example.ads.TestAdManager
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener

@Composable
fun TestBannerAd(
  creative: BannerCreative,
  onRotateCreative: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isRealAdLoaded by remember { mutableStateOf(false) }

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
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      // 1. Real Start.io Banner
      AndroidView(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        factory = { ctx ->
          val container = FrameLayout(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
            )
          }

          try {
            val startAppBanner = Banner(ctx, object : BannerListener {
              override fun onReceiveAd(banner: View?) {
                isRealAdLoaded = true
                TestAdManager.instance.logEvent("Banner", "Start.io Banner Loaded", AdConfig.STARTIO_APP_ID)
              }

              override fun onFailedToReceiveAd(banner: View?) {
                TestAdManager.instance.logEvent("Banner", "Start.io Banner Fill Pending", AdConfig.STARTIO_APP_ID)
              }

              override fun onClick(banner: View?) {
                TestAdManager.instance.logEvent("Banner", "Start.io Banner Clicked", AdConfig.STARTIO_APP_ID)
              }

              override fun onImpression(banner: View?) {
                isRealAdLoaded = true
                TestAdManager.instance.logEvent("Banner", "Start.io Banner Impression", AdConfig.STARTIO_APP_ID)
              }
            })
            container.addView(startAppBanner)
          } catch (e: Exception) {
            isRealAdLoaded = false
          }

          container
        },
        update = {
          // Container updated
        }
      )

      // 2. Banner placeholder if loading/network connecting
      if (!isRealAdLoaded) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(BentoSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
                  color = Color(0xFFFEF3C7)
                ) {
                  Text(
                    text = "START.IO",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "App ID: ${AdConfig.STARTIO_APP_ID}",
                  fontSize = 9.sp,
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
  }
}
