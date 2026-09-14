package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class MetronomeWorshipPreset(
    val bpm: Int,
    val title: String,
    val description: String
)

class WorshipMetronome {

    companion object {
        private const val SAMPLE_RATE = 22050
        val PRESETS = listOf(
            MetronomeWorshipPreset(60, "60 BPM", "Espontâneo / Oração Profunda"),
            MetronomeWorshipPreset(72, "72 BPM", "Louvor Intimista & Reflexivo"),
            MetronomeWorshipPreset(80, "80 BPM", "Balada Worship Congregacional"),
            MetronomeWorshipPreset(92, "92 BPM", "Worship Contemporâneo Dinâmico")
        )
    }

    private val isRunning = AtomicBoolean(false)
    private var workerThread: Thread? = null

    private val _bpm = MutableStateFlow(72)
    val bpm = _bpm.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    private val _currentBeat = MutableStateFlow(1)
    val currentBeat = _currentBeat.asStateFlow() // 1, 2, 3, 4

    private var tapTimes = mutableListOf<Long>()

    // Sound buffers for beat clicks
    private val clickAccent: ShortArray = generateClick(freqHz = 1600f, durationMs = 30)
    private val clickNormal: ShortArray = generateClick(freqHz = 900f, durationMs = 25)

    private var audioTrack: AudioTrack? = null

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf.coerceAtLeast(2048))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build().apply {
                play()
            }
    }

    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(40, 180)
    }

    fun toggle() {
        if (_isActive.value) {
            stop()
        } else {
            start()
        }
    }

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)
        _isActive.value = true

        workerThread = Thread({
            var beat = 1
            while (isRunning.get()) {
                val currentBpm = _bpm.value
                val beatIntervalMs = (60_000L / currentBpm).coerceAtLeast(250L)

                _currentBeat.value = beat

                // Play click
                val clickBuf = if (beat == 1) clickAccent else clickNormal
                audioTrack?.write(clickBuf, 0, clickBuf.size)

                beat = if (beat >= 4) 1 else beat + 1

                try {
                    Thread.sleep(beatIntervalMs)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }, "WorshipMetronomeThread").apply {
            priority = Thread.NORM_PRIORITY + 2
            start()
        }
    }

    fun stop() {
        isRunning.set(false)
        _isActive.value = false
        _currentBeat.value = 1
        workerThread?.interrupt()
        workerThread = null
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapTimes.add(now)
        // Keep only last 4 taps within 3 seconds
        tapTimes = tapTimes.filter { now - it < 3000 }.toMutableList()

        if (tapTimes.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimes.size) {
                intervals.add(tapTimes[i] - tapTimes[i - 1])
            }
            val avgInterval = intervals.average()
            if (avgInterval > 0) {
                val calculatedBpm = (60_000.0 / avgInterval).toInt().coerceIn(40, 180)
                _bpm.value = calculatedBpm
            }
        }
    }

    fun release() {
        stop()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    private fun generateClick(freqHz: Float, durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = exp(-t * 80f) // fast exponential decay
            val sample = (sin(2.0 * PI * freqHz * t) * envelope * 0.7f * 32767).toInt()
            buffer[i] = sample.coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }
}
