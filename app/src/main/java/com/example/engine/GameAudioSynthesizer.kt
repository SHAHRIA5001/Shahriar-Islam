package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object GameAudioSynthesizer {

  private val scope = CoroutineScope(Dispatchers.Default)
  private const val SAMPLE_RATE = 44100
  private var vibrator: Vibrator? = null

  fun init(context: Context) {
    if (vibrator == null) {
      vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
    }
  }

  fun vibrate(durationMs: Long = 35, amplitude: Int = 180, enabled: Boolean = true) {
    if (!enabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(durationMs)
      }
    } catch (_: Exception) { }
  }

  fun playSwipeSwoosh(soundEnabled: Boolean) {
    if (!soundEnabled) return
    scope.launch {
      // Rapid upward pitch sweep 280Hz -> 820Hz with soft attack/decay
      playToneSweep(startFreq = 280.0, endFreq = 820.0, durationMs = 120, volume = 0.55f)
    }
  }

  fun playImpactThud(soundEnabled: Boolean) {
    if (!soundEnabled) return
    scope.launch {
      // Low punchy saw wave 160Hz -> 45Hz
      playSawSweep(startFreq = 160.0, endFreq = 45.0, durationMs = 180, volume = 0.7f)
    }
  }

  fun playComboChime(comboLevel: Int, soundEnabled: Boolean) {
    if (!soundEnabled) return
    scope.launch {
      val baseFreq = when (comboLevel % 5) {
        1 -> 523.25 // C5
        2 -> 659.25 // E5
        3 -> 783.99 // G5
        4 -> 987.77 // B5
        else -> 1046.50 // C6
      }
      playChimeNote(freq = baseFreq, durationMs = 150, volume = 0.65f)
    }
  }

  fun playVictoryFanfare(soundEnabled: Boolean) {
    if (!soundEnabled) return
    scope.launch {
      // Arpeggiated victory chord C5 - E5 - G5 - C6
      playChimeNote(523.25, 90, 0.5f)
      playChimeNote(659.25, 90, 0.55f)
      playChimeNote(783.99, 120, 0.6f)
      playChimeNote(1046.50, 320, 0.7f)
    }
  }

  fun playFailSound(soundEnabled: Boolean) {
    if (!soundEnabled) return
    scope.launch {
      playToneSweep(380.0, 180.0, 240, 0.5f)
    }
  }

  fun playHintChime(soundEnabled: Boolean) {
    if (!soundEnabled) return
    scope.launch {
      playChimeNote(880.0, 100, 0.45f)
      playChimeNote(1318.51, 200, 0.55f)
    }
  }

  fun playButtonClick(soundEnabled: Boolean) {
    if (!soundEnabled) return
    scope.launch {
      playChimeNote(1100.0, 45, 0.35f)
    }
  }

  private fun playToneSweep(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float) {
    try {
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)
      var currentPhase = 0.0

      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        val currentFreq = startFreq + (endFreq - startFreq) * progress
        currentPhase += 2.0 * PI * currentFreq / SAMPLE_RATE

        // Exponential decay envelope
        val envelope = (1.0 - progress) * (if (progress < 0.1) progress / 0.1 else 1.0)
        val value = sin(currentPhase) * envelope * volume * Short.MAX_VALUE
        samples[i] = value.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }

      writeAndPlayAudio(samples)
    } catch (_: Exception) { }
  }

  private fun playSawSweep(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float) {
    try {
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)
      var phase = 0.0

      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        val currentFreq = startFreq + (endFreq - startFreq) * progress
        val phaseInc = currentFreq / SAMPLE_RATE
        phase = (phase + phaseInc) % 1.0

        val saw = (2.0 * phase - 1.0)
        val envelope = (1.0 - progress) * 0.85
        val value = saw * envelope * volume * Short.MAX_VALUE
        samples[i] = value.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }

      writeAndPlayAudio(samples)
    } catch (_: Exception) { }
  }

  private fun playChimeNote(freq: Double, durationMs: Int, volume: Float) {
    try {
      val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        val sinVal = sin(2.0 * PI * freq * i / SAMPLE_RATE)
        val harmonic = 0.3 * sin(2.0 * PI * (freq * 2.0) * i / SAMPLE_RATE)
        val envelope = (1.0 - progress) * (if (progress < 0.08) progress / 0.08 else 1.0)
        val value = (sinVal + harmonic) * envelope * volume * Short.MAX_VALUE
        samples[i] = value.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }

      writeAndPlayAudio(samples)
    } catch (_: Exception) { }
  }

  private fun writeAndPlayAudio(samples: ShortArray) {
    val minBufSize = AudioTrack.getMinBufferSize(
      SAMPLE_RATE,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT
    )
    val bufferSize = maxOf(minBufSize, samples.size * 2)

    val audioTrack = AudioTrack.Builder()
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_GAME)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )
      .setAudioFormat(
        AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(SAMPLE_RATE)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build()
      )
      .setBufferSizeInBytes(bufferSize)
      .setTransferMode(AudioTrack.MODE_STATIC)
      .build()

    audioTrack.write(samples, 0, samples.size)
    audioTrack.play()
    // Release automatically after playing
    audioTrack.setNotificationMarkerPosition(samples.size)
    audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
      override fun onMarkerReached(track: AudioTrack?) {
        try {
          track?.stop()
          track?.release()
        } catch (_: Exception) { }
      }
      override fun onPeriodicNotification(track: AudioTrack?) { }
    })
  }
}
