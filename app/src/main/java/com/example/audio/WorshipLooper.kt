package com.example.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

enum class LooperState {
    IDLE,
    RECORDING,
    PLAYING
}

class WorshipLooper {

    private val _state = MutableStateFlow(LooperState.IDLE)
    val state = _state.asStateFlow()

    private val _currentBar = MutableStateFlow(1)
    val currentBar = _currentBar.asStateFlow()

    private val _loopProgress = MutableStateFlow(0f)
    val loopProgress = _loopProgress.asStateFlow()

    private var loopBuffer = FloatArray(0)
    private var loopCapacitySamples = 0
    private var recordIndex = 0
    private var playbackIndex = 0

    @Volatile var looperVolume = 0.8f

    fun startRecord8Bars(bpm: Int) {
        // 8 bars of 4 beats = 32 beats
        val totalSeconds = (32.0 * 60.0) / bpm
        loopCapacitySamples = (totalSeconds * WorshipSynthEngine.SAMPLE_RATE).toInt()
        loopBuffer = FloatArray(loopCapacitySamples)
        recordIndex = 0
        playbackIndex = 0
        _currentBar.value = 1
        _loopProgress.value = 0f
        _state.value = LooperState.RECORDING
    }

    fun processAudio(inputBuffer: FloatArray, frameCount: Int): FloatArray? {
        val currentState = _state.value
        if (currentState == LooperState.IDLE || loopCapacitySamples == 0) return null

        val samplesPerBar = loopCapacitySamples / 8

        if (currentState == LooperState.RECORDING) {
            for (i in 0 until frameCount) {
                if (recordIndex < loopCapacitySamples) {
                    loopBuffer[recordIndex] = inputBuffer[i]
                    recordIndex++
                } else {
                    // Finished recording 8 bars -> transition directly to PLAYING loop
                    _state.value = LooperState.PLAYING
                    playbackIndex = 0
                    break
                }
            }
            val progress = recordIndex.toFloat() / loopCapacitySamples.toFloat()
            _loopProgress.value = progress.coerceIn(0f, 1f)
            val bar = (recordIndex / samplesPerBar) + 1
            _currentBar.value = bar.coerceIn(1, 8)
            return null
        } else if (currentState == LooperState.PLAYING) {
            val output = FloatArray(frameCount)
            for (i in 0 until frameCount) {
                output[i] = loopBuffer[playbackIndex] * looperVolume
                playbackIndex = (playbackIndex + 1) % loopCapacitySamples
            }
            val progress = playbackIndex.toFloat() / loopCapacitySamples.toFloat()
            _loopProgress.value = progress.coerceIn(0f, 1f)
            val bar = (playbackIndex / samplesPerBar) + 1
            _currentBar.value = bar.coerceIn(1, 8)
            return output
        }
        return null
    }

    fun stop() {
        _state.value = LooperState.IDLE
        recordIndex = 0
        playbackIndex = 0
        _loopProgress.value = 0f
        _currentBar.value = 1
    }

    fun togglePlayStop() {
        if (_state.value == LooperState.PLAYING) {
            _state.value = LooperState.IDLE
        } else if (loopBuffer.isNotEmpty() && recordIndex >= loopCapacitySamples) {
            _state.value = LooperState.PLAYING
            playbackIndex = 0
        }
    }

    fun clear() {
        stop()
        loopBuffer = FloatArray(0)
        loopCapacitySamples = 0
    }
}
