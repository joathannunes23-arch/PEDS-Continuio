package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import android.util.Log
import com.example.model.ArpPattern
import com.example.model.FilterType
import com.example.model.LfoDestination
import com.example.model.MinorScaleType
import com.example.model.OscWaveform
import com.example.model.Preset
import com.example.model.RootNote
import com.example.model.Tonality
import com.example.model.WorshipVoicing
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Ultra-stable, zero-allocation real-time stereo polyphonic synthesis engine.
 * Specifically tuned for Android AudioTrack with urgent audio thread priority,
 * glitch-free frequency transitions, denormal-safe resonant filtering,
 * and warm worship pad textures (Waldorf Blofeld / Serum inspired).
 */
class WorshipSynthEngine {

    companion object {
        const val TAG = "WorshipSynthEngine"
        const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE_FRAMES = 1024
        private const val NUM_VOICES = 6

        // Precalculated MIDI note frequencies (0..127) tuned to A440
        private val MIDI_NOTE_FREQS = DoubleArray(128) { note ->
            440.0 * Math.pow(2.0, (note - 69.0) / 12.0)
        }
    }

    private var audioTrack: AudioTrack? = null
    private var synthThread: Thread? = null
    private val isRunning = AtomicBoolean(false)

    // Current State
    val isPlaying = AtomicBoolean(false)
    val activeRootNote = AtomicReference<RootNote>(RootNote.C)
    val currentTonality = AtomicReference<Tonality>(Tonality.MAJOR)
    val minorScaleType = AtomicReference<MinorScaleType>(MinorScaleType.HARMONIC_MINOR)
    val worshipVoicing = AtomicReference<WorshipVoicing>(WorshipVoicing.SUS2_AMBIENT)
    val currentPreset = AtomicReference<Preset>()

    // Global Synth Parameters
    @Volatile var masterVolume: Float = 0.85f
    @Volatile var pitchBendSemitones: Float = 0.0f
    @Volatile var modWheel: Float = 0.0f // 0.0 to 1.0 (opens filter & shimmer)
    @Volatile var octaveShift: Int = 0 // -1, 0, +1
    @Volatile var tuningBaseHz: Float = 440.0f // 440Hz vs 432Hz
    @Volatile var fineDetuneCents: Float = 0.0f
    @Volatile var pan360: Float = 0.0f // -1.0 (Left) to +1.0 (Right)

    // Synth Rack Parameters
    @Volatile var cutoffHz: Float = 3500f
    @Volatile var resonance: Float = 1.2f
    @Volatile var filterType: FilterType = FilterType.LOW_PASS
    @Volatile var lfoRateHz: Float = 1.0f
    @Volatile var lfoDepth: Float = 0.25f
    @Volatile var lfoDestination: LfoDestination = LfoDestination.WAH

    // Slow Worship Arpeggiator
    @Volatile var isArpEnabled: Boolean = false
    @Volatile var arpSpeedSec: Float = 2.0f
    @Volatile var arpPattern: ArpPattern = ArpPattern.WORSHIP_FLOAT

    // Effects
    @Volatile var reverbWet: Float = 0.65f
    @Volatile var reverbDecay: Float = 0.8f
    @Volatile var shimmerLevel: Float = 0.5f
    @Volatile var delayTimeMs: Float = 380f
    @Volatile var delayFeedback: Float = 0.45f
    @Volatile var chorusDepth: Float = 0.5f

    // Category Volumes
    @Volatile var volumePads: Float = 1.0f
    @Volatile var volumeAmbient: Float = 1.0f
    @Volatile var volumeStrings: Float = 1.0f
    @Volatile var volumeProgressive: Float = 1.0f
    @Volatile var volumeWorshipFx: Float = 1.0f
    @Volatile var volumeChoral: Float = 1.0f

    // Performance Recorder Listener
    var audioOutputListener: ((FloatArray, Int) -> Unit)? = null

    // Real-time Visualizer callback for waveform & amplitude
    var visualizerCallback: ((FloatArray, Float) -> Unit)? = null

    // Internal DSP Voice State
    private val voicePhases = DoubleArray(NUM_VOICES)
    private val voicePhases2 = DoubleArray(NUM_VOICES) // for detuned supersaw
    private val voiceGains = FloatArray(NUM_VOICES) { 0f }
    private val currentVoiceFreqs = DoubleArray(NUM_VOICES) { 261.63 }
    private val targetVoiceFreqs = DoubleArray(NUM_VOICES) { 261.63 }

    private var lfoPhase: Double = 0.0
    private var arpTimerFrames: Long = 0
    private var currentArpStep: Int = 0

    // Biquad filter history & precalculated coefficients
    private var filterX1L = 0f; private var filterX2L = 0f
    private var filterY1L = 0f; private var filterY2L = 0f
    private var filterX1R = 0f; private var filterX2R = 0f
    private var filterY1R = 0f; private var filterY2R = 0f

    // Filter coefficients updated once per buffer
    private var b0 = 1f; private var b1 = 0f; private var b2 = 0f
    private var a1 = 0f; private var a2 = 0f

    // Delay line (stereo circular buffer)
    private val delayBufferSize = SAMPLE_RATE * 2 // 2 seconds max
    private val delayBufferL = FloatArray(delayBufferSize)
    private val delayBufferR = FloatArray(delayBufferSize)
    private var delayWriteIndex = 0

    // Chorus delay line
    private val chorusBufferSize = SAMPLE_RATE / 10 // 100ms
    private val chorusBufferL = FloatArray(chorusBufferSize)
    private val chorusBufferR = FloatArray(chorusBufferSize)
    private var chorusWriteIndex = 0
    private var chorusLfoPhase = 0.0

    // Reverb comb/allpass state (Stereo Freeverb-style with low-pass damping)
    private val combL = Array(4) { FloatArray(1600 + it * 280) }
    private val combR = Array(4) { FloatArray(1650 + it * 290) }
    private val combIndexL = IntArray(4)
    private val combIndexR = IntArray(4)
    private val combDampL = FloatArray(4)
    private val combDampR = FloatArray(4)

    // Smooth envelope state for pad on/off crossfading
    private var masterAmpEnvelope = 0.0f
    private var currentWaveform: OscWaveform = OscWaveform.WARM_SAW

    // Fast seed for zero-allocation noise generator
    private var noiseSeed: Int = 123456789

    fun startEngine() {
        if (isRunning.get()) return
        isRunning.set(true)

        val minBufSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        // Ensure generous headroom to completely prevent buffer underrun crackles
        val bufferSize = max(minBufSize * 3, BUFFER_SIZE_FRAMES * 4 * 4)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AudioTrack", e)
        }

        synthThread = Thread({ synthLoop() }, "WorshipSynthAudioThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stopEngine() {
        isRunning.set(false)
        isPlaying.set(false)
        try {
            synthThread?.join(500)
        } catch (_: Exception) {}
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        synthThread = null
    }

    fun applyPreset(preset: Preset) {
        currentPreset.set(preset)
        currentWaveform = preset.waveform
        cutoffHz = preset.cutoffHz
        resonance = preset.resonance
        filterType = preset.filterType
        shimmerLevel = preset.shimmerLevel
        reverbWet = preset.reverbWet
        reverbDecay = preset.reverbDecay
        delayTimeMs = preset.delayTimeMs
        delayFeedback = preset.delayFeedback
        chorusDepth = preset.chorusDepth
        isArpEnabled = preset.isArpEnabled
        arpSpeedSec = preset.arpeggiatorSpeedSec
        lfoRateHz = preset.lfoRateHz
        lfoDepth = preset.lfoDepth
        lfoDestination = preset.lfoDest
    }

    fun togglePlay(rootNote: RootNote? = null) {
        if (rootNote != null) {
            activeRootNote.set(rootNote)
        }
        val newState = !isPlaying.get()
        isPlaying.set(newState)
    }

    fun playNote(rootNote: RootNote) {
        activeRootNote.set(rootNote)
        isPlaying.set(true)
    }

    fun stopPlay() {
        isPlaying.set(false)
    }

    fun setTonality(tonality: Tonality) {
        currentTonality.set(tonality)
    }

    fun toggleTonality() {
        val current = currentTonality.get()
        currentTonality.set(if (current == Tonality.MAJOR) Tonality.MINOR else Tonality.MAJOR)
    }

    private fun getActiveChordIntervals(): IntArray {
        val tonality = currentTonality.get()
        val minorType = minorScaleType.get()

        return if (tonality == Tonality.MAJOR) {
            when (worshipVoicing.get()) {
                WorshipVoicing.FUNDAMENTAL -> intArrayOf(0, 7, 12, 19, 24, 7)
                WorshipVoicing.SUS2_AMBIENT -> intArrayOf(0, 2, 7, 12, 14, 19)
                WorshipVoicing.SUS4 -> intArrayOf(0, 5, 7, 12, 17, 19)
                WorshipVoicing.ADD9 -> intArrayOf(0, 4, 7, 14, 16, 19)
                WorshipVoicing.POWER_5 -> intArrayOf(-12, 0, 7, 12, 19, 24)
                WorshipVoicing.SEVENTH -> intArrayOf(0, 4, 7, 11, 14, 19)
            }
        } else {
            val third = 3 // minor 3rd
            val fifth = 7
            val seventh = when (minorType) {
                MinorScaleType.HARMONIC_MINOR,
                MinorScaleType.MELODIC_MINOR -> 11 // raised leading tone
                MinorScaleType.NATURAL_MINOR -> 10 // minor 7th
            }

            when (worshipVoicing.get()) {
                WorshipVoicing.FUNDAMENTAL -> intArrayOf(0, third, fifth, 12, 12 + third, 19)
                WorshipVoicing.SUS2_AMBIENT -> intArrayOf(0, 2, third, fifth, 12 + third, 19)
                WorshipVoicing.SUS4 -> intArrayOf(0, 5, fifth, 12, 12 + third, 19)
                WorshipVoicing.ADD9 -> intArrayOf(0, third, fifth, 14, 12 + third, 19)
                WorshipVoicing.POWER_5 -> intArrayOf(-12, 0, fifth, 12, 12 + third, 24)
                WorshipVoicing.SEVENTH -> intArrayOf(0, third, fifth, seventh, 12 + third, 19)
            }
        }
    }

    private fun synthLoop() {
        // Set OS-level urgent audio priority for zero scheduling jitter
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        } catch (_: Exception) {}

        val pcmBuffer = ShortArray(BUFFER_SIZE_FRAMES * 2) // Stereo (L, R)
        val floatBufferL = FloatArray(BUFFER_SIZE_FRAMES)
        val floatBufferR = FloatArray(BUFFER_SIZE_FRAMES)
        val visBuffer = FloatArray(BUFFER_SIZE_FRAMES / 4)

        var visDecimateCounter = 0

        while (isRunning.get()) {
            val track = audioTrack ?: break

            val playing = isPlaying.get()
            val targetAmp = if (playing) 1.0f else 0.0f
            val isEngineAudible = (masterAmpEnvelope > 0.0001f || targetAmp > 0f)

            if (!isEngineAudible) {
                // Write clean silence to keep AudioTrack clock synchronized without sleep jitter
                pcmBuffer.fill(0)
                track.write(pcmBuffer, 0, pcmBuffer.size)
                continue
            }

            val chordIntervals = getActiveChordIntervals()
            val rootOffset = activeRootNote.get().midiOffset
            val baseMidi = 48 + rootOffset + (octaveShift * 12) + pitchBendSemitones

            // Fine tuning ratio (440Hz / 432Hz + fine detune)
            val tuningRatio = (tuningBaseHz / 440.0) * Math.pow(2.0, fineDetuneCents.toDouble() / 1200.0)

            // Update target voice frequencies with smooth portamento
            for (v in 0 until NUM_VOICES) {
                val interval = chordIntervals[v % chordIntervals.size]
                val midiNote = (baseMidi + interval).toInt().coerceIn(0, 127)
                targetVoiceFreqs[v] = MIDI_NOTE_FREQS[midiNote] * tuningRatio
            }

            val arpIntervalFrames = ((arpSpeedSec * SAMPLE_RATE) / chordIntervals.size).toInt().coerceAtLeast(1000)

            val categoryVol = when (currentPreset.get()?.category) {
                null, com.example.model.SoundCategory.PADS -> volumePads
                com.example.model.SoundCategory.AMBIENT -> volumeAmbient
                com.example.model.SoundCategory.STRINGS -> volumeStrings
                com.example.model.SoundCategory.PROGRESSIVE -> volumeProgressive
                com.example.model.SoundCategory.WORSHIP_FX -> volumeWorshipFx
                com.example.model.SoundCategory.CHORAL -> volumeChoral
            }

            val effVol = masterVolume * categoryVol

            // Compute biquad filter coefficients ONCE PER BUFFER
            var effectiveCutoff = cutoffHz + (modWheel * 3000f)
            if (lfoDestination == LfoDestination.WAH) {
                val lfoVal = (sin(lfoPhase).toFloat() * lfoDepth).coerceIn(-1f, 1f)
                effectiveCutoff *= (1.0f + lfoVal * 0.6f).coerceIn(0.2f, 2.5f)
            }
            effectiveCutoff = effectiveCutoff.coerceIn(120f, 16000f)
            computeFilterCoefficients(effectiveCutoff, resonance.coerceIn(0.5f, 4.0f), filterType)

            val currentWf = currentWaveform
            val curShimmer = (shimmerLevel + modWheel * 0.5f).coerceIn(0f, 1f)
            val curChorusDepth = chorusDepth
            val curDelayTime = delayTimeMs
            val curDelayFeedback = delayFeedback
            val curReverbWet = reverbWet
            val curReverbDecay = reverbDecay

            // 360 Pan calculation
            val panL = (1.0f - pan360.coerceIn(-1f, 1f)) * 0.5f + 0.5f
            val panR = (1.0f + pan360.coerceIn(-1f, 1f)) * 0.5f + 0.5f

            val lfoInc = 2.0 * PI * lfoRateHz / SAMPLE_RATE

            // Render audio frames with zero heap allocations
            for (i in 0 until BUFFER_SIZE_FRAMES) {
                // Envelope crossfade (smooth attack and release)
                val envStep = if (targetAmp > masterAmpEnvelope) 0.003f else 0.001f
                masterAmpEnvelope += (targetAmp - masterAmpEnvelope) * envStep

                // Advance LFO
                lfoPhase += lfoInc
                if (lfoPhase > 2.0 * PI) lfoPhase -= 2.0 * PI
                val lfoValue = sin(lfoPhase).toFloat() * lfoDepth

                // Arpeggiator stepping
                if (isArpEnabled) {
                    arpTimerFrames++
                    if (arpTimerFrames >= arpIntervalFrames) {
                        arpTimerFrames = 0
                        currentArpStep = when (arpPattern) {
                            ArpPattern.UP -> (currentArpStep + 1) % NUM_VOICES
                            ArpPattern.DOWN -> (currentArpStep - 1 + NUM_VOICES) % NUM_VOICES
                            ArpPattern.UP_DOWN -> {
                                val s = (currentArpStep + 1) % (NUM_VOICES * 2)
                                if (s < NUM_VOICES) s else (NUM_VOICES * 2 - 1 - s)
                            }
                            ArpPattern.WORSHIP_FLOAT -> ((fastRandom() * NUM_VOICES).toInt()).coerceIn(0, NUM_VOICES - 1)
                        }
                    }
                }

                var voiceSumL = 0f
                var voiceSumR = 0f

                for (v in 0 until NUM_VOICES) {
                    // Smooth frequency portamento (eliminates pitch clicks)
                    currentVoiceFreqs[v] += (targetVoiceFreqs[v] - currentVoiceFreqs[v]) * 0.005

                    var freq = currentVoiceFreqs[v]
                    if (lfoDestination == LfoDestination.VIBRATO) {
                        freq *= (1.0 + lfoValue * 0.02)
                    }

                    val phaseIncr = (2.0 * PI * freq) / SAMPLE_RATE
                    val phaseIncrDetuned = phaseIncr * 1.004

                    voicePhases[v] += phaseIncr
                    if (voicePhases[v] > 2.0 * PI) voicePhases[v] -= 2.0 * PI

                    voicePhases2[v] += phaseIncrDetuned
                    if (voicePhases2[v] > 2.0 * PI) voicePhases2[v] -= 2.0 * PI

                    // Arp voice attenuation
                    val voiceArpWeight = if (isArpEnabled) {
                        if (v == currentArpStep) 1.2f else 0.35f
                    } else 1.0f

                    voiceGains[v] += (voiceArpWeight - voiceGains[v]) * 0.005f

                    // Zero-allocation inline oscillator synthesis
                    val p1 = voicePhases[v]
                    val p2 = voicePhases2[v]

                    var sA = 0f
                    var sB = 0f

                    when (currentWf) {
                        OscWaveform.WARM_SAW -> {
                            sA = (1.0 - (p1 / PI)).toFloat()
                            sB = (1.0 - (p2 / PI)).toFloat()
                        }
                        OscWaveform.SUPER_SAW -> {
                            val saw1 = (1.0 - (p1 / PI)).toFloat()
                            val saw2 = (1.0 - (p2 / PI)).toFloat()
                            val sub = sin(p1 * 0.5).toFloat() * 0.35f
                            sA = saw1 + sub
                            sB = saw2 + sub
                        }
                        OscWaveform.ORGAN_SINE -> {
                            sA = sin(p1).toFloat()
                            sB = (sin(p1 * 2.0).toFloat() * 0.35f) + (sin(p1 * 0.5).toFloat() * 0.5f)
                        }
                        OscWaveform.CHOIR_FORMANT -> {
                            val car = sin(p1).toFloat()
                            val f1 = sin(p1 * 2.8).toFloat() * 0.5f
                            val f2 = sin(p1 * 4.6).toFloat() * 0.3f
                            val air = (fastRandom() * 2f - 1f) * 0.04f
                            sA = car + f1 + air
                            sB = car + f2 + air
                        }
                        OscWaveform.ATMOS_NOISE -> {
                            val noise = (fastRandom() * 2f - 1f) * 0.35f
                            val tone = sin(p1).toFloat() * 0.65f
                            sA = tone + noise
                            sB = tone + noise
                        }
                        OscWaveform.BELL_SHIMMER -> {
                            val f = sin(p1).toFloat()
                            val oct = sin(p1 * 2.0).toFloat() * 0.5f
                            val fifth = sin(p1 * 3.0).toFloat() * 0.35f
                            val chime = sin(p1 * 6.0).toFloat() * 0.2f
                            sA = f + fifth
                            sB = oct + chime
                        }
                    }

                    val shimmerSample = sin(p1 * 2.0).toFloat() * 0.3f * curShimmer
                    val monoVoice = (sA * 0.6f + sB * 0.4f + shimmerSample) * voiceGains[v]

                    // Stereo spread per voice
                    val panVoice = ((v - (NUM_VOICES - 1) / 2.0f) / NUM_VOICES.toFloat()) * 0.5f
                    val leftPan = (0.5f - panVoice).coerceIn(0f, 1f)
                    val rightPan = (0.5f + panVoice).coerceIn(0f, 1f)

                    voiceSumL += monoVoice * leftPan
                    voiceSumR += monoVoice * rightPan
                }

                // Tremolo LFO
                if (lfoDestination == LfoDestination.TREMOLO) {
                    val trem = (1.0f + lfoValue * 0.5f).coerceIn(0f, 1.5f)
                    voiceSumL *= trem
                    voiceSumR *= trem
                }

                // In-place Biquad Filtering (Denormal protected)
                val fltL = filterSampleL(voiceSumL)
                val fltR = filterSampleR(voiceSumR)

                // In-place Chorus
                var chL = fltL
                var chR = fltR
                if (curChorusDepth > 0.05f) {
                    chorusLfoPhase += 2.0 * PI * 1.2 / SAMPLE_RATE
                    if (chorusLfoPhase > 2.0 * PI) chorusLfoPhase -= 2.0 * PI

                    val modL = (sin(chorusLfoPhase).toFloat() * 0.5f + 0.5f) * curChorusDepth * (chorusBufferSize * 0.6f)
                    val modR = (cos(chorusLfoPhase).toFloat() * 0.5f + 0.5f) * curChorusDepth * (chorusBufferSize * 0.6f)

                    chorusBufferL[chorusWriteIndex] = fltL
                    chorusBufferR[chorusWriteIndex] = fltR

                    val readIdxL = (chorusWriteIndex - modL.toInt() + chorusBufferSize) % chorusBufferSize
                    val readIdxR = (chorusWriteIndex - modR.toInt() + chorusBufferSize) % chorusBufferSize

                    chorusWriteIndex = (chorusWriteIndex + 1) % chorusBufferSize

                    chL = fltL * (1f - curChorusDepth * 0.35f) + chorusBufferL[readIdxL] * (curChorusDepth * 0.35f)
                    chR = fltR * (1f - curChorusDepth * 0.35f) + chorusBufferR[readIdxR] * (curChorusDepth * 0.35f)
                }

                // In-place Ping-Pong Delay
                var dlyL = chL
                var dlyR = chR
                if (curDelayFeedback > 0.05f) {
                    val delaySamples = ((curDelayTime / 1000f) * SAMPLE_RATE).toInt().coerceIn(100, delayBufferSize - 10)
                    val rIdxL = (delayWriteIndex - delaySamples + delayBufferSize) % delayBufferSize
                    val rIdxR = (delayWriteIndex - (delaySamples * 3 / 4) + delayBufferSize) % delayBufferSize

                    val delOutL = delayBufferL[rIdxL]
                    val delOutR = delayBufferR[rIdxR]

                    delayBufferL[delayWriteIndex] = softClip(chL + delOutR * curDelayFeedback.coerceIn(0f, 0.8f))
                    delayBufferR[delayWriteIndex] = softClip(chR + delOutL * curDelayFeedback.coerceIn(0f, 0.8f))

                    delayWriteIndex = (delayWriteIndex + 1) % delayBufferSize

                    dlyL = chL + delOutL * 0.35f
                    dlyR = chR + delOutR * 0.35f
                }

                // In-place Damped Reverb (comb filters with low-pass damping)
                var revL = dlyL
                var revR = dlyR
                if (curReverbWet > 0.05f) {
                    val effDecay = curReverbDecay.coerceIn(0.5f, 0.92f)
                    var sumL = 0f
                    var sumR = 0f

                    for (k in 0 until 4) {
                        val bufL = combL[k]
                        val bufR = combR[k]
                        val idxL = combIndexL[k]
                        val idxR = combIndexR[k]

                        val outL = bufL[idxL]
                        val outR = bufR[idxR]

                        // Simple one-pole lowpass damping inside feedback to eliminate harsh ringing
                        combDampL[k] = outL * 0.4f + combDampL[k] * 0.6f
                        combDampR[k] = outR * 0.4f + combDampR[k] * 0.6f

                        bufL[idxL] = dlyL + combDampL[k] * effDecay
                        bufR[idxR] = dlyR + combDampR[k] * effDecay

                        combIndexL[k] = (idxL + 1) % bufL.size
                        combIndexR[k] = (idxR + 1) % bufR.size

                        sumL += outL
                        sumR += outR
                    }

                    sumL *= 0.25f
                    sumR *= 0.25f

                    val wet = curReverbWet.coerceIn(0f, 1f)
                    val dry = 1f - wet * 0.5f
                    revL = dlyL * dry + sumL * wet
                    revR = dlyR * dry + sumR * wet
                }

                // Master gain & soft saturation
                var finalL = revL * effVol * masterAmpEnvelope * panL
                var finalR = revR * effVol * masterAmpEnvelope * panR

                finalL = softClip(finalL)
                finalR = softClip(finalR)

                floatBufferL[i] = finalL
                floatBufferR[i] = finalR

                // Convert to 16-bit PCM
                pcmBuffer[i * 2] = (finalL * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                pcmBuffer[i * 2 + 1] = (finalR * 32767f).toInt().coerceIn(-32768, 32767).toShort()
            }

            // Write PCM directly to Android AudioTrack
            track.write(pcmBuffer, 0, pcmBuffer.size)

            // Feed performance recorder
            audioOutputListener?.invoke(floatBufferL, BUFFER_SIZE_FRAMES)

            // Visualizer callback throttled to ~20 FPS (every 2 buffers)
            visDecimateCounter++
            if (visDecimateCounter % 2 == 0) {
                var maxRms = 0f
                for (j in visBuffer.indices) {
                    val idx = j * 4
                    val sample = (floatBufferL[idx] + floatBufferR[idx]) * 0.5f
                    visBuffer[j] = sample
                    val sampleAbs = abs(sample)
                    if (sampleAbs > maxRms) maxRms = sampleAbs
                }
                visualizerCallback?.invoke(visBuffer, maxRms)
            }
        }
    }

    private fun computeFilterCoefficients(cutoff: Float, q: Float, type: FilterType) {
        val omega = 2.0 * PI * cutoff / SAMPLE_RATE
        val alpha = sin(omega) / (2.0 * max(0.1f, q))
        val cosW = cos(omega)

        var rawB0 = 1.0; var rawB1 = 0.0; var rawB2 = 0.0
        var rawA0 = 1.0; var rawA1 = 0.0; var rawA2 = 0.0

        when (type) {
            FilterType.LOW_PASS -> {
                rawB0 = (1.0 - cosW) / 2.0
                rawB1 = 1.0 - cosW
                rawB2 = (1.0 - cosW) / 2.0
                rawA0 = 1.0 + alpha
                rawA1 = -2.0 * cosW
                rawA2 = 1.0 - alpha
            }
            FilterType.HIGH_PASS -> {
                rawB0 = (1.0 + cosW) / 2.0
                rawB1 = -(1.0 + cosW)
                rawB2 = (1.0 + cosW) / 2.0
                rawA0 = 1.0 + alpha
                rawA1 = -2.0 * cosW
                rawA2 = 1.0 - alpha
            }
            FilterType.BAND_PASS -> {
                rawB0 = alpha
                rawB1 = 0.0
                rawB2 = -alpha
                rawA0 = 1.0 + alpha
                rawA1 = -2.0 * cosW
                rawA2 = 1.0 - alpha
            }
        }

        b0 = (rawB0 / rawA0).toFloat()
        b1 = (rawB1 / rawA0).toFloat()
        b2 = (rawB2 / rawA0).toFloat()
        a1 = (rawA1 / rawA0).toFloat()
        a2 = (rawA2 / rawA0).toFloat()
    }

    private fun filterSampleL(inL: Float): Float {
        // Denormal / NaN protection
        if (filterY1L.isNaN() || filterY1L.isInfinite() || abs(filterY1L) > 8f) {
            filterX1L = 0f; filterX2L = 0f; filterY1L = 0f; filterY2L = 0f
        }
        val outL = b0 * inL + b1 * filterX1L + b2 * filterX2L - a1 * filterY1L - a2 * filterY2L
        filterX2L = filterX1L
        filterX1L = inL
        filterY2L = filterY1L
        filterY1L = outL
        return outL
    }

    private fun filterSampleR(inR: Float): Float {
        // Denormal / NaN protection
        if (filterY1R.isNaN() || filterY1R.isInfinite() || abs(filterY1R) > 8f) {
            filterX1R = 0f; filterX2R = 0f; filterY1R = 0f; filterY2R = 0f
        }
        val outR = b0 * inR + b1 * filterX1R + b2 * filterX2R - a1 * filterY1R - a2 * filterY2R
        filterX2R = filterX1R
        filterX1R = inR
        filterY2R = filterY1R
        filterY1R = outR
        return outR
    }

    private fun fastRandom(): Float {
        noiseSeed = (noiseSeed * 1664525 + 1013904223)
        return ((noiseSeed ushr 8) and 0x00FFFFFF).toFloat() / 16777216.0f
    }

    private fun softClip(x: Float): Float {
        return if (x > 3f) 1f else if (x < -3f) -1f else (x * (27f + x * x) / (27f + 9f * x * x))
    }
}

