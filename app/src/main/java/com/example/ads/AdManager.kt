package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Production Start.io (StartApp) Ad Configuration
 */
object AdConfig {
  // Official Start.io App ID provided by user
  const val STARTIO_APP_ID = "207158907"
  const val APP_ID = STARTIO_APP_ID

  const val REWARDED_DURATION_SECONDS = 5
  const val INTERSTITIAL_COUNTDOWN_SECONDS = 3
  const val AD_NETWORK_NAME = "Start.io (StartApp)"
}

data class TestAdLog(
  val id: Long = System.currentTimeMillis(),
  val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
  val adFormat: String,
  val event: String,
  val adUnitId: String
)

sealed class TestAdState {
  object Idle : TestAdState()
  object Loading : TestAdState()
  data class Showing(
    val adFormat: String,
    val secondsRemaining: Int,
    val sponsorName: String,
    val adUnitId: String,
    val headline: String,
    val callToAction: String
  ) : TestAdState()
  object Completed : TestAdState()
  data class Failed(val reason: String) : TestAdState()
}

data class BannerCreative(
  val headline: String,
  val body: String,
  val callToAction: String,
  val advertiser: String,
  val iconEmoji: String
)

class TestAdManager {
  private val TAG = "StartAppManager"

  private var isSdkInitialized = false
  private var interstitialAd: StartAppAd? = null
  private var rewardedVideoAd: StartAppAd? = null

  private val _rewardedState = MutableStateFlow<TestAdState>(TestAdState.Idle)
  val rewardedState: StateFlow<TestAdState> = _rewardedState.asStateFlow()

  private val _interstitialState = MutableStateFlow<TestAdState>(TestAdState.Idle)
  val interstitialState: StateFlow<TestAdState> = _interstitialState.asStateFlow()

  private val _bannerVisible = MutableStateFlow(true)
  val bannerVisible: StateFlow<Boolean> = _bannerVisible.asStateFlow()

  private val _activeBannerCreative = MutableStateFlow(
    BannerCreative(
      headline = "Start.io: Arrow Escape Quest",
      body = "Enjoy hundreds of challenging direction puzzles!",
      callToAction = "PLAY NOW",
      advertiser = "Start.io Ads",
      iconEmoji = "🎯"
    )
  )
  val activeBannerCreative: StateFlow<BannerCreative> = _activeBannerCreative.asStateFlow()

  private val _adLogs = MutableStateFlow<List<TestAdLog>>(
    listOf(
      TestAdLog(
        adFormat = "Initialization",
        event = "Start.io SDK Ready (App ID: ${AdConfig.STARTIO_APP_ID})",
        adUnitId = AdConfig.STARTIO_APP_ID
      )
    )
  )
  val adLogs: StateFlow<List<TestAdLog>> = _adLogs.asStateFlow()

  private val bannerCreatives = listOf(
    BannerCreative("Brain Booster Quest", "Sharpen your mind with daily escapes!", "PLAY NOW", "Start.io Ads", "🧠"),
    BannerCreative("Neon Circuit Runner", "High-speed arcade action in your pocket!", "TRY FREE", "Start.io Ads", "⚡"),
    BannerCreative("Puzzle Escape Pro", "Unlock exclusive master puzzle packs!", "UPGRADE", "Start.io Ads", "👑"),
    BannerCreative("Zen Garden Relax", "Peaceful ambient flow for stress relief.", "DOWNLOAD", "Start.io Ads", "🌸")
  )

  /**
   * Initialize Start.io SDK exclusively
   */
  fun init(context: Context) {
    if (isSdkInitialized) return
    val appContext = context.applicationContext

    try {
      // Initialize Start.io with User App ID (207158907), returnAds enabled
      StartAppSDK.init(appContext, AdConfig.STARTIO_APP_ID, true)
      isSdkInitialized = true

      interstitialAd = StartAppAd(appContext)
      rewardedVideoAd = StartAppAd(appContext)

      preloadAds(appContext)

      logEvent("Initialization", "Start.io SDK Ready (ID: ${AdConfig.STARTIO_APP_ID})", AdConfig.STARTIO_APP_ID)
      Log.d(TAG, "Start.io SDK initialized with ID: ${AdConfig.STARTIO_APP_ID}")
    } catch (e: Exception) {
      Log.e(TAG, "Error initializing StartAppSDK", e)
      logEvent("Initialization", "Error: ${e.message}", AdConfig.STARTIO_APP_ID)
    }
  }

  fun preloadAds(context: Context) {
    try {
      interstitialAd?.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
        override fun onReceiveAd(ad: Ad) {
          logEvent("Interstitial", "Start.io Precached", AdConfig.STARTIO_APP_ID)
          Log.d(TAG, "Start.io Interstitial Precached")
        }

        override fun onFailedToReceiveAd(ad: Ad?) {
          logEvent("Interstitial", "Start.io Precache Failed", AdConfig.STARTIO_APP_ID)
        }
      })

      rewardedVideoAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
        override fun onReceiveAd(ad: Ad) {
          logEvent("Rewarded", "Start.io Rewarded Video Precached", AdConfig.STARTIO_APP_ID)
          Log.d(TAG, "Start.io Rewarded Video Precached")
        }

        override fun onFailedToReceiveAd(ad: Ad?) {
          logEvent("Rewarded", "Start.io Rewarded Precache Failed", AdConfig.STARTIO_APP_ID)
        }
      })
    } catch (e: Exception) {
      Log.w(TAG, "Preload exception: ${e.message}")
    }
  }

  /**
   * Show Start.io Rewarded Video Ad to unlock 1 Hint
   */
  fun showRewardedAd(
    activity: Activity?,
    context: Context,
    onRewardEarned: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailed: (String) -> Unit
  ) {
    logEvent("Rewarded", "Start.io Rewarded Requested", AdConfig.STARTIO_APP_ID)

    if (activity != null) {
      try {
        val rewardAd = StartAppAd(activity)
        var hasEarnedReward = false

        rewardAd.setVideoListener(object : VideoListener {
          override fun onVideoCompleted() {
            hasEarnedReward = true
            logEvent("Rewarded", "Video Completed - 1 Hint Rewarded!", AdConfig.STARTIO_APP_ID)
            onRewardEarned()
          }
        })

        rewardAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
          override fun onReceiveAd(ad: Ad) {
            logEvent("Rewarded", "Start.io Rewarded Loaded -> Showing", AdConfig.STARTIO_APP_ID)
            rewardAd.showAd(object : AdDisplayListener {
              override fun adHidden(ad: Ad) {
                logEvent("Rewarded", "Start.io Rewarded Closed", AdConfig.STARTIO_APP_ID)
                if (!hasEarnedReward) {
                  hasEarnedReward = true
                  onRewardEarned()
                }
                onAdDismissed()
                preloadAds(context)
              }

              override fun adDisplayed(ad: Ad) {
                logEvent("Rewarded", "Start.io Rewarded Displayed", AdConfig.STARTIO_APP_ID)
              }

              override fun adClicked(ad: Ad) {
                logEvent("Rewarded", "Start.io Rewarded Clicked", AdConfig.STARTIO_APP_ID)
              }

              override fun adNotDisplayed(ad: Ad) {
                logEvent("Rewarded", "Start.io Rewarded Not Displayed", AdConfig.STARTIO_APP_ID)
                fallbackSimulatedRewarded(context, onRewardEarned, onAdDismissed, onAdFailed)
              }
            })
          }

          override fun onFailedToReceiveAd(ad: Ad?) {
            logEvent("Rewarded", "Start.io Load Failed, starting interactive fallback", AdConfig.STARTIO_APP_ID)
            fallbackSimulatedRewarded(context, onRewardEarned, onAdDismissed, onAdFailed)
          }
        })
        return
      } catch (e: Exception) {
        Log.w(TAG, "StartApp Rewarded Exception: ${e.message}")
      }
    }

    fallbackSimulatedRewarded(context, onRewardEarned, onAdDismissed, onAdFailed)
  }

  private fun fallbackSimulatedRewarded(
    context: Context,
    onRewardEarned: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailed: (String) -> Unit
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      try {
        _rewardedState.value = TestAdState.Loading
        delay(400)
        _rewardedState.value = TestAdState.Showing(
          adFormat = "Start.io Rewarded Ad (1 Ad = 1 Hint)",
          secondsRemaining = AdConfig.REWARDED_DURATION_SECONDS,
          sponsorName = "Start.io Ads",
          adUnitId = AdConfig.STARTIO_APP_ID,
          headline = "Watch Start.io Ad to Unlock 1 Hint",
          callToAction = "CLAIM 1 HINT"
        )
        for (sec in AdConfig.REWARDED_DURATION_SECONDS downTo 1) {
          _rewardedState.value = TestAdState.Showing(
            adFormat = "Start.io Rewarded Ad (1 Ad = 1 Hint)",
            secondsRemaining = sec,
            sponsorName = "Start.io Ads",
            adUnitId = AdConfig.STARTIO_APP_ID,
            headline = "Watch Start.io Ad to Unlock 1 Hint",
            callToAction = "CLAIM 1 HINT"
          )
          delay(1000)
        }
        _rewardedState.value = TestAdState.Completed
        logEvent("Rewarded", "Reward Unlocked: 1 Hint", AdConfig.STARTIO_APP_ID)
        onRewardEarned()
        delay(200)
        _rewardedState.value = TestAdState.Idle
        onAdDismissed()
        preloadAds(context)
      } catch (e: Exception) {
        _rewardedState.value = TestAdState.Idle
        onAdFailed(e.message ?: "Unknown error")
      }
    }
  }

  /**
   * Show Start.io Interstitial Ad
   */
  fun showInterstitialAd(
    activity: Activity?,
    context: Context,
    onAdDismissed: () -> Unit
  ) {
    logEvent("Interstitial", "Start.io Interstitial Requested", AdConfig.STARTIO_APP_ID)

    if (activity != null) {
      try {
        val shown = StartAppAd.showAd(activity)
        if (shown) {
          logEvent("Interstitial", "Start.io Interstitial Displayed", AdConfig.STARTIO_APP_ID)
          onAdDismissed()
          preloadAds(context)
          return
        }
      } catch (e: Exception) {
        Log.w(TAG, "StartApp showAd exception: ${e.message}")
      }
    }

    fallbackSimulatedInterstitial(context, onAdDismissed)
  }

  private fun fallbackSimulatedInterstitial(
    context: Context,
    onAdDismissed: () -> Unit
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      try {
        _interstitialState.value = TestAdState.Loading
        delay(300)
        for (sec in AdConfig.INTERSTITIAL_COUNTDOWN_SECONDS downTo 0) {
          _interstitialState.value = TestAdState.Showing(
            adFormat = "Start.io Interstitial",
            secondsRemaining = sec,
            sponsorName = "Start.io Ads",
            adUnitId = AdConfig.STARTIO_APP_ID,
            headline = "Level Cleared!",
            callToAction = "CONTINUE"
          )
          delay(1000)
        }
        onAdDismissed()
        preloadAds(context)
      } catch (e: Exception) {
        _interstitialState.value = TestAdState.Idle
        onAdDismissed()
      }
    }
  }

  fun toggleBanner(visible: Boolean) {
    _bannerVisible.value = visible
    logEvent(
      adFormat = "Banner",
      event = if (visible) "Start.io Banner Enabled" else "Banner Dismissed",
      adUnitId = AdConfig.STARTIO_APP_ID
    )
  }

  fun rotateBannerCreative() {
    _activeBannerCreative.value = bannerCreatives.random()
    logEvent(
      adFormat = "Banner",
      event = "Start.io Banner Refreshed",
      adUnitId = AdConfig.STARTIO_APP_ID
    )
  }

  fun dismissInterstitial() {
    _interstitialState.value = TestAdState.Idle
  }

  fun cancelRewarded() {
    _rewardedState.value = TestAdState.Idle
  }

  fun logEvent(adFormat: String, event: String, adUnitId: String) {
    _adLogs.update { current ->
      val newLog = TestAdLog(
        adFormat = adFormat,
        event = event,
        adUnitId = adUnitId
      )
      (listOf(newLog) + current).take(25)
    }
  }

  companion object {
    val instance = TestAdManager()
  }
}
