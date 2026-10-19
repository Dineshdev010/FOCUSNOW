package com.focusflow.app.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.focusflow.app.model.FocusSoundscape
import kotlin.concurrent.thread
import kotlin.math.sin
import kotlin.random.Random

object FocusSoundManager {
    @Volatile
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var soundThread: Thread? = null
    private var currentSoundscape = FocusSoundscape.SILENT

    fun play(soundscape: FocusSoundscape) {
        stop()
        if (soundscape == FocusSoundscape.SILENT) {
            currentSoundscape = FocusSoundscape.SILENT
            return
        }
        currentSoundscape = soundscape
        isPlaying = true

        soundThread = thread(start = true, isDaemon = true, name = "FocusSoundThread") {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = (minBufferSize * 2).coerceAtLeast(8192)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                val buffer = ShortArray(bufferSize / 2)
                var sampleIndex = 0L
                var filterState = 0.0

                while (isPlaying) {
                    for (i in 0 until buffer.size step 2) {
                        val t = sampleIndex.toDouble() / sampleRate
                        when (soundscape) {
                            FocusSoundscape.BINAURAL_BEATS -> {
                                // 200 Hz Left, 240 Hz Right -> 40 Hz Gamma difference for cognitive focus
                                val left = (sin(2.0 * Math.PI * 200.0 * t) * 6000.0).toInt().toShort()
                                val right = (sin(2.0 * Math.PI * 240.0 * t) * 6000.0).toInt().toShort()
                                buffer[i] = left
                                buffer[i + 1] = right
                            }
                            FocusSoundscape.RAIN -> {
                                // Low-pass filtered noise to emulate soothing rain
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                filterState = filterState * 0.94 + white * 0.06
                                val sample = (filterState * 9000.0).toInt().coerceIn(-32768, 32767).toShort()
                                buffer[i] = sample
                                buffer[i + 1] = sample
                            }
                            FocusSoundscape.FOREST -> {
                                // 136.1 Hz OM meditation tone with gentle harmonic
                                val fundamental = sin(2.0 * Math.PI * 136.1 * t)
                                val harmonic = sin(2.0 * Math.PI * 272.2 * t) * 0.3
                                val sample = ((fundamental + harmonic) * 4500.0).toInt().toShort()
                                buffer[i] = sample
                                buffer[i + 1] = sample
                            }
                            FocusSoundscape.WAVES -> {
                                // Modulated ocean wave wash (period of 6 seconds)
                                val waveMod = (sin(2.0 * Math.PI * 0.166 * t) + 1.0) / 2.0
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                filterState = filterState * 0.90 + white * 0.10
                                val sample = (filterState * waveMod * 10000.0).toInt().coerceIn(-32768, 32767).toShort()
                                buffer[i] = sample
                                buffer[i + 1] = sample
                            }
                            FocusSoundscape.SILENT -> {
                                buffer[i] = 0
                                buffer[i + 1] = 0
                            }
                        }
                        sampleIndex++
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Graceful stop on thread interrupt or track release
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (ignored: Exception) {}
                audioTrack = null
            }
        }
    }

    fun stop() {
        isPlaying = false
        currentSoundscape = FocusSoundscape.SILENT
        try {
            soundThread?.interrupt()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (ignored: Exception) {}
        soundThread = null
        audioTrack = null
    }

    fun isCurrentlyPlaying(): Boolean = isPlaying && currentSoundscape != FocusSoundscape.SILENT
    fun getActiveSoundscape(): FocusSoundscape = currentSoundscape
}