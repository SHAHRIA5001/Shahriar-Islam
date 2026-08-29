package com.example.ads

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Google Mobile Ads Production & Sandbox Configuration with User AdMob IDs
 */
object AdConfig {
  // Official AdMob App ID
  const val APP_ID = "ca-app-pub-2138009485514699~2101153339"
  const val TEST_APP_ID = APP_ID

  // Official AdMob Ad Unit IDs provided by User
  const val BANNER_ID = "ca-app-pub-2138009485514699/2101153339"
  const val BANNER_TEST_UNIT_ID = BANNER_ID

  const val INTERSTITIAL_ID = "ca-app-pub-2138009485514699/4872560409"
  const val INTERSTITIAL_TEST_UNIT_ID = INTERSTITIAL_ID

  const val REWARDED_INTERSTITIAL_ID = "ca-app-pub-2138009485514699/3397390309"
  const val REWARDED_INTERSTITIAL_TEST_UNIT_ID = REWARDED_INTERSTITIAL_ID

  const val REWARDED_ID = "ca-app-pub-2138009485514699/3924003913"
  const val REWARDED_TEST_UNIT_ID = REWARDED_ID

  const val NATIVE_ADVANCED_ID = "ca-app-pub-2138009485514699/2013929943"
  const val NATIVE_ADVANCED_TEST_UNIT_ID = NATIVE_ADVANCED_ID

  const val APP_OPEN_ID = "ca-app-pub-2138009485514699/5100976913"
  const val APP_OPEN_TEST_UNIT_ID = APP_OPEN_ID

  const val REWARDED_DURATION_SECONDS = 5
  const val INTERSTITIAL_COUNTDOWN_SECONDS = 3
  const val AD_NETWORK_NAME = "Google AdMob"
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
  private val _rewardedState = MutableStateFlow<TestAdState>(TestAdState.Idle)
  val rewardedState: StateFlow<TestAdState> = _rewardedState.asStateFlow()

  private val _interstitialState = MutableStateFlow<TestAdState>(TestAdState.Idle)
  val interstitialState: StateFlow<TestAdState> = _interstitialState.asStateFlow()

  private val _bannerVisible = MutableStateFlow(true)
  val bannerVisible: StateFlow<Boolean> = _bannerVisible.asStateFlow()

  private val _activeBannerCreative = MutableStateFlow(
    BannerCreative(
      headline = "Test Ad: Super Arrow 3D",
      body = "Experience 200+ challenging grid puzzles!",
      callToAction = "INSTALL",
      advertiser = "Google AdMob Sandbox",
      iconEmoji = "🎯"
    )
  )
  val activeBannerCreative: StateFlow<BannerCreative> = _activeBannerCreative.asStateFlow()

  private val _adLogs = MutableStateFlow<List<TestAdLog>>(
    listOf(
      TestAdLog(
        adFormat = "Initialization",
        event = "Google Mobile Ads SDK Initialized (Test Suite)",
        adUnitId = AdConfig.TEST_APP_ID
      )
    )
  )
  val adLogs: StateFlow<List<TestAdLog>> = _adLogs.asStateFlow()

  private val bannerCreatives = listOf(
    BannerCreative("Test Ad: Brain Booster Quest", "Sharpen your mind with daily escapes!", "PLAY NOW", "Google AdMob Test", "🧠"),
    BannerCreative("Test Ad: Neon Circuit Runner", "High-speed arcade action in your pocket!", "TRY FREE", "AdMob Sandbox", "⚡"),
    BannerCreative("Test Ad: Puzzle Escape Pro", "Unlock exclusive master puzzle packs!", "UPGRADE", "Google Ads Test", "👑"),
    BannerCreative("Test Ad: Zen Garden Relax", "Peaceful ambient flow for stress relief.", "DOWNLOAD", "AdMob Test Suite", "🌸")
  )

  fun toggleBanner(visible: Boolean) {
    _bannerVisible.value = visible
    logEvent(
      adFormat = "Banner",
      event = if (visible) "Banner Display Enabled" else "Banner Dismissed by User",
      adUnitId = AdConfig.BANNER_TEST_UNIT_ID
    )
  }

  fun rotateBannerCreative() {
    _activeBannerCreative.value = bannerCreatives.random()
    logEvent(
      adFormat = "Banner",
      event = "onAdLoaded (Refreshed 320x50 Smart Banner)",
      adUnitId = AdConfig.BANNER_TEST_UNIT_ID
    )
  }

  suspend fun showRewardedAd(
    onRewardEarned: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailed: (String) -> Unit
  ) {
    try {
      logEvent("Rewarded", "loadAd() requested", AdConfig.REWARDED_TEST_UNIT_ID)
      _rewardedState.value = TestAdState.Loading
      delay(350)
      logEvent("Rewarded", "onAdLoaded()", AdConfig.REWARDED_TEST_UNIT_ID)
      logEvent("Rewarded", "onAdImpression()", AdConfig.REWARDED_TEST_UNIT_ID)

      val sponsors = listOf(
        "Cosmic Arrow Pro",
        "Master Puzzle Academy",
        "Neon Logic Games",
        "Apex Mind Lab"
      )
      val sponsor = sponsors.random()

      for (sec in AdConfig.REWARDED_DURATION_SECONDS downTo 1) {
        _rewardedState.value = TestAdState.Showing(
          adFormat = "Rewarded Video",
          secondsRemaining = sec,
          sponsorName = sponsor,
          adUnitId = AdConfig.REWARDED_TEST_UNIT_ID,
          headline = "Watch & Earn +3 Free Hint Tickets",
          callToAction = "CLAIM REWARD"
        )
        delay(1000)
      }

      _rewardedState.value = TestAdState.Completed
      logEvent("Rewarded", "onUserEarnedReward(type=HintTickets, amount=3)", AdConfig.REWARDED_TEST_UNIT_ID)
      onRewardEarned()
      delay(250)
      _rewardedState.value = TestAdState.Idle
      logEvent("Rewarded", "onAdDismissedFullScreenContent()", AdConfig.REWARDED_TEST_UNIT_ID)
      onAdDismissed()
    } catch (e: Exception) {
      val reason = e.localizedMessage ?: "Network Timeout"
      logEvent("Rewarded", "onAdFailedToLoad(error=$reason)", AdConfig.REWARDED_TEST_UNIT_ID)
      _rewardedState.value = TestAdState.Failed(reason)
      onAdFailed(reason)
      delay(400)
      _rewardedState.value = TestAdState.Idle
      onAdDismissed()
    }
  }

  suspend fun showInterstitialAd(
    onAdDismissed: () -> Unit
  ) {
    try {
      logEvent("Interstitial", "loadAd() requested", AdConfig.INTERSTITIAL_TEST_UNIT_ID)
      _interstitialState.value = TestAdState.Loading
      delay(300)
      logEvent("Interstitial", "onAdLoaded()", AdConfig.INTERSTITIAL_TEST_UNIT_ID)
      logEvent("Interstitial", "onAdImpression()", AdConfig.INTERSTITIAL_TEST_UNIT_ID)

      for (sec in AdConfig.INTERSTITIAL_COUNTDOWN_SECONDS downTo 0) {
        _interstitialState.value = TestAdState.Showing(
          adFormat = "Interstitial",
          secondsRemaining = sec,
          sponsorName = "AdMob Test Network",
          adUnitId = AdConfig.INTERSTITIAL_TEST_UNIT_ID,
          headline = "Level Cleared! Test Interstitial Ad",
          callToAction = "LEARN MORE"
        )
        delay(1000)
      }
    } catch (e: Exception) {
      logEvent("Interstitial", "onAdFailedToShowFullScreenContent()", AdConfig.INTERSTITIAL_TEST_UNIT_ID)
      _interstitialState.value = TestAdState.Idle
      onAdDismissed()
    }
  }

  fun dismissInterstitial() {
    logEvent("Interstitial", "onAdDismissedFullScreenContent()", AdConfig.INTERSTITIAL_TEST_UNIT_ID)
    _interstitialState.value = TestAdState.Idle
  }

  fun cancelRewarded() {
    _rewardedState.value = TestAdState.Idle
  }

  private fun logEvent(adFormat: String, event: String, adUnitId: String) {
    _adLogs.update { current ->
      val newLog = TestAdLog(
        adFormat = adFormat,
        event = event,
        adUnitId = adUnitId
      )
      (listOf(newLog) + current).take(20)
    }
  }

  companion object {
    val instance = TestAdManager()
  }
}
