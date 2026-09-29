package com.example.echoshift.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.echoshift.model.Biome
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

object AudioEngine {
    private const val SAMPLE_RATE = 22050
    private var isPlayingMusic = false
    private var musicJob: Job? = null
    private var coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var sfxVolume: Float = 0.8f
    var musicVolume: Float = 0.5f

    enum class SoundType {
        JUMP,
        DASH,
        ECHO_START,
        ECHO_SPAWN,
        ECHO_SWAP,
        SWITCH_CLICK,
        COLLECTIBLE,
        DAMAGE,
        ENERGY_PULSE,
        VICTORY,
        GAME_OVER
    }

    fun playSound(type: SoundType) {
        if (sfxVolume <= 0.01f) return
        coroutineScope.launch {
            try {
                val samples = generateSfxSamples(type)
                playPcmSamples(samples, sfxVolume)
            } catch (_: Exception) {
            }
        }
    }

    private fun generateSfxSamples(type: SoundType): ShortArray {
        return when (type) {
            SoundType.JUMP -> {
                // Pitch slide 240Hz -> 620Hz, 120ms
                val duration = (SAMPLE_RATE * 0.12).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val freq = 240f + (380f * (i.toFloat() / duration))
                    val env = 1f - (i.toFloat() / duration)
                    val s = sin(2 * Math.PI * freq * t).toFloat()
                    (s * env * 32767 * 0.4f).toInt().toShort()
                }
            }
            SoundType.DASH -> {
                // Whoosh: White noise mixed with descending square wave
                val duration = (SAMPLE_RATE * 0.15).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / duration
                    val env = sin(t * Math.PI).toFloat()
                    val noise = (Math.random() * 2 - 1).toFloat()
                    val wave = sin(2 * Math.PI * (800f - 500f * t) * (i.toFloat() / SAMPLE_RATE)).toFloat()
                    val mixed = (noise * 0.7f + wave * 0.3f) * env
                    (mixed * 32767 * 0.45f).toInt().toShort()
                }
            }
            SoundType.ECHO_START -> {
                // Cyber chime 440Hz -> 880Hz dual tone
                val duration = (SAMPLE_RATE * 0.20).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val progress = i.toFloat() / duration
                    val freq = 440f + 440f * progress
                    val env = (1f - progress)
                    val s = (sin(2 * Math.PI * freq * t) * 0.7f + sin(2 * Math.PI * (freq * 1.5) * t) * 0.3f).toFloat()
                    (s * env * 32767 * 0.5f).toInt().toShort()
                }
            }
            SoundType.ECHO_SPAWN -> {
                // Harmonic temporal release: 330Hz, 660Hz chord with sweep
                val duration = (SAMPLE_RATE * 0.25).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val env = (1f - (i.toFloat() / duration))
                    val s = (sin(2 * Math.PI * 330 * t) + sin(2 * Math.PI * 495 * t) + sin(2 * Math.PI * 660 * t)) / 3f
                    (s * env * 32767 * 0.5f).toInt().toShort()
                }
            }
            SoundType.ECHO_SWAP -> {
                // Fast phase warp 1200Hz down to 300Hz with tremolo
                val duration = (SAMPLE_RATE * 0.18).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val p = i.toFloat() / duration
                    val freq = 1200f - 900f * p
                    val tremolo = sin(2 * Math.PI * 30 * t).toFloat()
                    val s = sin(2 * Math.PI * freq * t).toFloat() * (0.8f + 0.2f * tremolo)
                    val env = 1f - p
                    (s * env * 32767 * 0.5f).toInt().toShort()
                }
            }
            SoundType.SWITCH_CLICK -> {
                // Sharp mechanical cyber click: 1200Hz pulse, 40ms
                val duration = (SAMPLE_RATE * 0.04).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val env = 1f - (i.toFloat() / duration)
                    val s = (if (sin(2 * Math.PI * 1400 * t) > 0) 1f else -1f) * 0.5f
                    (s * env * 32767 * 0.4f).toInt().toShort()
                }
            }
            SoundType.COLLECTIBLE -> {
                // Sparkle chord: 523Hz (C5), 659Hz (E5), 783Hz (G5), 1046Hz (C6) fast arpeggio
                val duration = (SAMPLE_RATE * 0.3).toInt()
                val step = duration / 4
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val freq = when (i / step) {
                        0 -> 523.25f
                        1 -> 659.25f
                        2 -> 783.99f
                        else -> 1046.50f
                    }
                    val env = 1f - (i.toFloat() / duration)
                    val s = sin(2 * Math.PI * freq * t).toFloat()
                    (s * env * 32767 * 0.45f).toInt().toShort()
                }
            }
            SoundType.DAMAGE -> {
                // Low thud & crunch
                val duration = (SAMPLE_RATE * 0.2).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val env = 1f - (i.toFloat() / duration)
                    val noise = (Math.random() * 2 - 1).toFloat()
                    val bass = sin(2 * Math.PI * 90 * t).toFloat()
                    val mixed = (bass * 0.6f + noise * 0.4f) * env
                    (mixed * 32767 * 0.6f).toInt().toShort()
                }
            }
            SoundType.ENERGY_PULSE -> {
                // High-tech pulse discharge
                val duration = (SAMPLE_RATE * 0.25).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val p = i.toFloat() / duration
                    val freq = 400f + 800f * (1f - p)
                    val env = sin(p * Math.PI).toFloat()
                    val s = sin(2 * Math.PI * freq * t).toFloat()
                    (s * env * 32767 * 0.5f).toInt().toShort()
                }
            }
            SoundType.VICTORY -> {
                // Radiant triumphant arpeggio sequence
                val duration = (SAMPLE_RATE * 0.8).toInt()
                val noteDur = duration / 5
                val notes = floatArrayOf(440f, 554.37f, 659.25f, 880f, 1108.7f)
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val noteIdx = (i / noteDur).coerceIn(0, 4)
                    val noteProgress = (i % noteDur).toFloat() / noteDur
                    val env = (1f - noteProgress) * 0.8f
                    val s = (sin(2 * Math.PI * notes[noteIdx] * t) + sin(2 * Math.PI * (notes[noteIdx] * 2) * t) * 0.3f).toFloat()
                    (s * env * 32767 * 0.4f).toInt().toShort()
                }
            }
            SoundType.GAME_OVER -> {
                // Descending minor chord
                val duration = (SAMPLE_RATE * 0.6).toInt()
                ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val p = i.toFloat() / duration
                    val freq = 260f - 120f * p
                    val env = 1f - p
                    val s = sin(2 * Math.PI * freq * t).toFloat()
                    (s * env * 32767 * 0.5f).toInt().toShort()
                }
            }
        }
    }

    private fun playPcmSamples(samples: ShortArray, volume: Float) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBufferSize, samples.size * 2)

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

        audioTrack.setVolume(volume.coerceIn(0f, 1f))
        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()

        coroutineScope.launch {
            delay((samples.size * 1000L / SAMPLE_RATE) + 100)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }

    fun startMusic(biome: Biome, isBoss: Boolean = false) {
        stopMusic()
        if (musicVolume <= 0.01f) return
        isPlayingMusic = true
        musicJob = coroutineScope.launch {
            playMusicLoop(biome, isBoss)
        }
    }

    fun stopMusic() {
        isPlayingMusic = false
        musicJob?.cancel()
        musicJob = null
    }

    private suspend fun playMusicLoop(biome: Biome, isBoss: Boolean) {
        // Base chords depending on biome
        val chordProgressions = when {
            isBoss -> listOf(
                floatArrayOf(110f, 164.8f, 220f),  // A2 minor tense
                floatArrayOf(123.47f, 185f, 246.9f), // B2
                floatArrayOf(98f, 146.8f, 196f),    // G2
                floatArrayOf(103.8f, 155.5f, 207.6f) // G#2
            )
            biome == Biome.NEO_GRID || biome == Biome.APEX_STATION -> listOf(
                floatArrayOf(130.8f, 196f, 261.6f), // C3
                floatArrayOf(116.5f, 174.6f, 233.1f), // Bb2
                floatArrayOf(146.8f, 220f, 293.6f), // D3
                floatArrayOf(130.8f, 196f, 261.6f)  // C3
            )
            biome == Biome.CRYO_WASTES -> listOf(
                floatArrayOf(146.8f, 220f, 329.6f), // D3 crystal
                floatArrayOf(164.8f, 246.9f, 329.6f), // E3
                floatArrayOf(130.8f, 196f, 293.6f), // C3
                floatArrayOf(146.8f, 220f, 329.6f)
            )
            else -> listOf(
                floatArrayOf(110f, 164.8f, 220f),   // A2 ambient
                floatArrayOf(130.8f, 196f, 261.6f), // C3
                floatArrayOf(146.8f, 220f, 293.6f), // D3
                floatArrayOf(123.47f, 185f, 246.9f) // B2
            )
        }

        val tempoMs = if (isBoss) 240L else 380L
        var chordIdx = 0

        while (isPlayingMusic && kotlin.coroutines.coroutineContext.isActive) {
            val chord = chordProgressions[chordIdx % chordProgressions.size]
            chordIdx++

            // Play 4-beat arpeggio
            for (beat in 0..3) {
                if (!isPlayingMusic || !kotlin.coroutines.coroutineContext.isActive) break
                val note = chord[beat % chord.size] * (if (beat % 2 == 1) 2f else 1f)
                val duration = (SAMPLE_RATE * (tempoMs / 1000f)).toInt()
                val samples = ShortArray(duration) { i ->
                    val t = i.toFloat() / SAMPLE_RATE
                    val p = i.toFloat() / duration
                    val env = (1f - p) * 0.4f
                    val s = (sin(2 * Math.PI * note * t) + sin(2 * Math.PI * (note * 0.5) * t) * 0.5f).toFloat()
                    (s * env * 32767 * 0.25f * musicVolume).toInt().toShort()
                }
                playPcmSamples(samples, musicVolume)
                delay(tempoMs)
            }
        }
    }
}

object HapticsManager {
    fun vibrate(context: Context, durationMs: Long = 40L) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }
}
