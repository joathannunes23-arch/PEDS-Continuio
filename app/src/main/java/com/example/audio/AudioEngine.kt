package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * AudioEngine
 *
 * Comprehensive Kotlin Audio Engine managing FluidSynth integration,
 * SoundFont bank loading, and real-time audio routing into the Android AudioTrack subsystem.
 *
 * Highlights:
 * 1. FluidSynth Native Binding & Lifecycle:
 *    Manages fluid_settings_t and fluid_synth_t pointers, supporting both JNI shared libraries
 *    (libfluidsynth.so / libfluidsynth-jni.so) and an autonomous real-time fallback DSP engine.
 * 2. SoundFont Management:
 *    Handles dynamic loading, bank selection, preset enumeration, and unloading of .sf2 files.
 * 3. Android Audio Routing:
 *    Routes low-latency 16-bit interleaved stereo PCM audio directly to the Android media system
 *    via an optimized AudioTrack instance with urgent audio thread priority.
 * 4. MIDI & Worship Control:
 *    Exposes polyphonic noteOn, noteOff, sustain, pitch bend, modulation wheel, and reverb/chorus routing.
 */
class AudioEngine private constructor() {

    companion object {
        const val TAG = "AudioEngine"

        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_BUFFER_FRAMES = 512
        const val DEFAULT_POLYPHONY = 64
        const val MIDI_CHANNELS = 16

        @Volatile
        private var instance: AudioEngine? = null

        /**
         * Singleton accessor to ensure single-instance audio hardware resource binding.
         */
        fun getInstance(): AudioEngine {
            return instance ?: synchronized(this) {
                instance ?: AudioEngine().also { instance = it }
            }
        }
    }

    // ==========================================
    // Engine State & Observability
    // ==========================================
    enum class State {
        UNINITIALIZED,
        INITIALIZING,
        READY,
        RUNNING,
        STOPPED,
        ERROR,
        RELEASED
    }

    data class SoundFontPresetInfo(
        val soundFontId: Int,
        val bank: Int,
        val program: Int,
        val name: String
    )

    data class LoadedSoundFont(
        val id: Int,
        val filePath: String,
        val fileName: String,
        val fileSize: Long,
        val presets: List<SoundFontPresetInfo>
    )

    data class AudioRoutingStats(
        val sampleRate: Int = DEFAULT_SAMPLE_RATE,
        val bufferFrames: Int = DEFAULT_BUFFER_FRAMES,
        val latencyMs: Float = 0f,
        val totalFramesRouted: Long = 0L,
        val underruns: Int = 0,
        val isNativeBackend: Boolean = false,
        val activeVoices: Int = 0
    )

    private val _engineState = MutableStateFlow(State.UNINITIALIZED)
    val engineState: StateFlow<State> = _engineState.asStateFlow()

    private val _routingStats = MutableStateFlow(AudioRoutingStats())
    val routingStats: StateFlow<AudioRoutingStats> = _routingStats.asStateFlow()

    private val _loadedSoundFonts = MutableStateFlow<Map<Int, LoadedSoundFont>>(emptyMap())
    val loadedSoundFonts: StateFlow<Map<Int, LoadedSoundFont>> = _loadedSoundFonts.asStateFlow()

    private val _activeSoundFontId = MutableStateFlow<Int?>(null)
    val activeSoundFontId: StateFlow<Int?> = _activeSoundFontId.asStateFlow()

    // Native pointers (managed as 64-bit longs)
    private var fluidSettingsPtr: Long = 0L
    private var fluidSynthPtr: Long = 0L
    private var isNativeFluidSynthLoaded = false

    // SoundFont tracking
    private val nextSoundFontId = AtomicInteger(1)
    private val soundFontsMap = ConcurrentHashMap<Int, LoadedSoundFont>()

    // Audio routing components
    private var audioTrack: AudioTrack? = null
    private var audioRenderThread: Thread? = null
    private val isAudioRoutingActive = AtomicBoolean(false)

    // Audio rendering configuration
    @Volatile var sampleRate: Int = DEFAULT_SAMPLE_RATE
        private set
    @Volatile var bufferFrames: Int = DEFAULT_BUFFER_FRAMES
        private set
    @Volatile var masterVolume: Float = 0.85f
    @Volatile var masterGain: Float = 1.0f

    // Fallback DSP Renderer for zero-crash resilience
    private val fallbackRenderer = FallbackFluidSynthRenderer()

    init {
        detectAndLoadNativeLibraries()
    }

    /**
     * Checks if native FluidSynth libraries are bundled and loads them gracefully.
     */
    private fun detectAndLoadNativeLibraries() {
        try {
            System.loadLibrary("fluidsynth")
            System.loadLibrary("fluidsynth-jni")
            isNativeFluidSynthLoaded = true
            Log.i(TAG, "Native FluidSynth libraries loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            isNativeFluidSynthLoaded = false
            Log.w(
                TAG,
                "FluidSynth native binaries (.so) not found in runtime APK. " +
                        "Operating with internal resilient DSP SoundFont fallback engine."
            )
        }
    }

    // ==========================================
    // Lifecycle Management
    // ==========================================

    /**
     * Initializes the FluidSynth engine with custom sample rate, buffer size, and polyphony.
     * Sets up fluid_settings and initializes fluid_synth instance.
     */
    @Synchronized
    fun initialize(
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bufferFrames: Int = DEFAULT_BUFFER_FRAMES,
        polyphony: Int = DEFAULT_POLYPHONY
    ): Boolean {
        if (_engineState.value == State.READY || _engineState.value == State.RUNNING) {
            Log.d(TAG, "AudioEngine is already initialized.")
            return true
        }

        _engineState.value = State.INITIALIZING
        this.sampleRate = sampleRate
        this.bufferFrames = bufferFrames

        try {
            if (isNativeFluidSynthLoaded) {
                initNativeFluidSynth(sampleRate, bufferFrames, polyphony)
            } else {
                fallbackRenderer.initialize(sampleRate, polyphony)
            }

            setupAndroidAudioTrack(sampleRate, bufferFrames)

            _engineState.value = State.READY
            updateStats()
            Log.i(TAG, "AudioEngine initialized successfully at ${sampleRate}Hz, buffer: $bufferFrames frames.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioEngine", e)
            _engineState.value = State.ERROR
            return false
        }
    }

    private fun initNativeFluidSynth(sampleRate: Int, bufferFrames: Int, polyphony: Int) {
        fluidSettingsPtr = FluidSynthBindings.new_fluid_settings()
        if (fluidSettingsPtr == 0L) {
            throw IllegalStateException("Unable to allocate fluid_settings_t.")
        }

        // Configure optimal settings for Android real-time media output
        FluidSynthBindings.fluid_settings_setnum(fluidSettingsPtr, "synth.sample-rate", sampleRate.toDouble())
        FluidSynthBindings.fluid_settings_setint(fluidSettingsPtr, "synth.polyphony", polyphony)
        FluidSynthBindings.fluid_settings_setint(fluidSettingsPtr, "synth.midi-channels", MIDI_CHANNELS)
        FluidSynthBindings.fluid_settings_setnum(fluidSettingsPtr, "synth.gain", 0.8)
        FluidSynthBindings.fluid_settings_setint(fluidSettingsPtr, "synth.reverb.active", 1)
        FluidSynthBindings.fluid_settings_setint(fluidSettingsPtr, "synth.chorus.active", 1)
        FluidSynthBindings.fluid_settings_setint(fluidSettingsPtr, "audio.period-size", bufferFrames)
        FluidSynthBindings.fluid_settings_setint(fluidSettingsPtr, "audio.periods", 2)

        fluidSynthPtr = FluidSynthBindings.new_fluid_synth(fluidSettingsPtr)
        if (fluidSynthPtr == 0L) {
            FluidSynthBindings.delete_fluid_settings(fluidSettingsPtr)
            fluidSettingsPtr = 0L
            throw IllegalStateException("Unable to allocate fluid_synth_t.")
        }
    }

    /**
     * Configures the Android AudioTrack subsystem for low-latency stereo streaming.
     */
    private fun setupAndroidAudioTrack(sampleRate: Int, bufferFrames: Int) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val targetBufferSize = max(minBufferSize, bufferFrames * 2 * 2) // 2 channels * 2 bytes/sample

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(targetBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()
    }

    // ==========================================
    // SoundFont Management
    // ==========================================

    /**
     * Loads a SoundFont file (.sf2) into the FluidSynth instance.
     *
     * @param soundFontPath Absolute local file system path to the .sf2 file.
     * @param resetPresets Whether existing channel presets should be reset.
     * @return Result containing LoadedSoundFont metadata or failure.
     */
    @Synchronized
    fun loadSoundFont(soundFontPath: String, resetPresets: Boolean = true): Result<LoadedSoundFont> {
        val file = File(soundFontPath)
        if (!file.exists() || !file.canRead()) {
            return Result.failure(IllegalArgumentException("SoundFont file does not exist or cannot be read: $soundFontPath"))
        }

        try {
            val soundFontId: Int
            val presetsList: List<SoundFontPresetInfo>

            if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
                val nativeId = FluidSynthBindings.fluid_synth_sfload(
                    fluidSynthPtr,
                    file.absolutePath,
                    if (resetPresets) 1 else 0
                )
                if (nativeId < 0) {
                    return Result.failure(IllegalStateException("FluidSynth failed to load SoundFont: $soundFontPath (code: $nativeId)"))
                }
                soundFontId = nativeId
                presetsList = enumerateSoundFontPresets(file, soundFontId)
            } else {
                // Fallback internal engine handling
                soundFontId = nextSoundFontId.getAndIncrement()
                presetsList = enumerateSoundFontPresets(file, soundFontId)
                fallbackRenderer.registerSoundFont(soundFontId, file, presetsList)
            }

            val loaded = LoadedSoundFont(
                id = soundFontId,
                filePath = file.absolutePath,
                fileName = file.name,
                fileSize = file.length(),
                presets = presetsList
            )

            soundFontsMap[soundFontId] = loaded
            _loadedSoundFonts.value = soundFontsMap.toMap()
            _activeSoundFontId.value = soundFontId

            // Default route channel 0 to the first preset
            selectSoundFontPreset(
                channel = 0,
                soundFontId = soundFontId,
                bank = presetsList.firstOrNull()?.bank ?: 0,
                program = presetsList.firstOrNull()?.program ?: 0
            )

            Log.i(TAG, "SoundFont loaded: '${file.name}' with ID $soundFontId (${presetsList.size} presets found)")
            return Result.success(loaded)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading SoundFont from path: $soundFontPath", e)
            return Result.failure(e)
        }
    }

    /**
     * Unloads a previously registered SoundFont by ID.
     */
    @Synchronized
    fun unloadSoundFont(soundFontId: Int): Boolean {
        val item = soundFontsMap.remove(soundFontId) ?: return false

        var success = true
        if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
            val code = FluidSynthBindings.fluid_synth_sfunload(fluidSynthPtr, soundFontId, 1)
            success = code == 0
        } else {
            fallbackRenderer.unregisterSoundFont(soundFontId)
        }

        _loadedSoundFonts.value = soundFontsMap.toMap()
        if (_activeSoundFontId.value == soundFontId) {
            _activeSoundFontId.value = soundFontsMap.keys.firstOrNull()
        }

        Log.i(TAG, "SoundFont ID $soundFontId ('${item.fileName}') unloaded. Success: $success")
        return success
    }

    /**
     * Selects a specific bank and program (preset) from a loaded SoundFont onto a MIDI channel.
     */
    fun selectSoundFontPreset(channel: Int, soundFontId: Int, bank: Int, program: Int): Boolean {
        if (!soundFontsMap.containsKey(soundFontId)) {
            Log.w(TAG, "Cannot select preset: SoundFont ID $soundFontId is not loaded.")
            return false
        }

        if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
            val result = FluidSynthBindings.fluid_synth_program_select(
                fluidSynthPtr,
                channel,
                soundFontId,
                bank,
                program
            )
            return result == 0
        } else {
            fallbackRenderer.selectPreset(channel, soundFontId, bank, program)
            return true
        }
    }

    /**
     * Fast parser for SoundFont 2.04 chunk architecture (RIFF, LIST, pdta, phdr) to extract preset names.
     */
    private fun enumerateSoundFontPresets(file: File, sfontId: Int): List<SoundFontPresetInfo> {
        val presets = mutableListOf<SoundFontPresetInfo>()
        try {
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(min(file.length(), 1024 * 512).toInt())
                val read = fis.read(buffer)
                if (read > 64) {
                    val header = String(buffer, 0, 4)
                    if (header == "RIFF") {
                        val format = String(buffer, 8, 4)
                        if (format == "sfbk") {
                            // Extract preset headers if parseable or generate standard worship pad bank listings
                            presets.addAll(parseSf2PresetChunks(buffer, read, sfontId))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error inspecting SoundFont presets directly, generating default bank mapping", e)
        }

        if (presets.isEmpty()) {
            val baseName = file.nameWithoutExtension.replace("_", " ")
            presets.add(SoundFontPresetInfo(sfontId, 0, 0, "$baseName - Main Pad"))
            presets.add(SoundFontPresetInfo(sfontId, 0, 1, "$baseName - Bright Air"))
            presets.add(SoundFontPresetInfo(sfontId, 0, 2, "$baseName - Warm Sus2"))
            presets.add(SoundFontPresetInfo(sfontId, 0, 3, "$baseName - Shimmer Halo"))
        }

        return presets
    }

    private fun parseSf2PresetChunks(data: ByteArray, length: Int, sfontId: Int): List<SoundFontPresetInfo> {
        val list = mutableListOf<SoundFontPresetInfo>()
        var offset = 12
        while (offset + 8 < length) {
            val chunkId = String(data, offset, 4)
            val chunkSize = ByteBuffer.wrap(data, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (chunkSize < 0 || offset + 8 + chunkSize > length) break

            if (chunkId == "LIST") {
                val listType = String(data, offset + 8, 4)
                if (listType == "pdta") {
                    var subOffset = offset + 12
                    val subEnd = offset + 8 + chunkSize
                    while (subOffset + 8 < subEnd) {
                        val subChunkId = String(data, subOffset, 4)
                        val subChunkSize = ByteBuffer.wrap(data, subOffset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
                        if (subChunkId == "phdr" && subChunkSize >= 38) {
                            var recordOffset = subOffset + 8
                            val recordEnd = recordOffset + subChunkSize
                            while (recordOffset + 38 <= recordEnd) {
                                val nameBytes = ByteArray(20)
                                System.arraycopy(data, recordOffset, nameBytes, 0, 20)
                                val nullIdx = nameBytes.indexOf(0)
                                val name = String(nameBytes, 0, if (nullIdx >= 0) nullIdx else 20).trim()

                                val presetNum = ByteBuffer.wrap(data, recordOffset + 20, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
                                val bankNum = ByteBuffer.wrap(data, recordOffset + 22, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF

                                if (name.isNotEmpty() && name != "EOP") {
                                    list.add(SoundFontPresetInfo(sfontId, bankNum, presetNum, name))
                                }
                                recordOffset += 38
                            }
                            return list
                        }
                        subOffset += 8 + ((subChunkSize + 1) and 1.inv())
                    }
                }
            }
            offset += 8 + ((chunkSize + 1) and 1.inv())
        }
        return list
    }

    // ==========================================
    // Audio Signal Routing to Android AudioTrack
    // ==========================================

    /**
     * Starts the real-time audio routing thread.
     * Continuously requests synthesized audio frames from FluidSynth and writes them to the Android AudioTrack.
     */
    @Synchronized
    fun startAudioRouting(): Boolean {
        if (_engineState.value != State.READY && _engineState.value != State.STOPPED) {
            Log.w(TAG, "AudioEngine is not ready to start routing. Current state: ${_engineState.value}")
            return false
        }

        val track = audioTrack ?: run {
            Log.e(TAG, "AudioTrack has not been initialized.")
            return false
        }

        try {
            track.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AudioTrack playback", e)
            return false
        }

        isAudioRoutingActive.set(true)

        audioRenderThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            Log.i(TAG, "Urgent audio routing pump started on thread: ${Thread.currentThread().name}")

            val frameCount = bufferFrames
            val shortBuffer = ShortArray(frameCount * 2) // Interleaved stereo: L, R, L, R...
            val leftBuf = ShortArray(frameCount)
            val rightBuf = ShortArray(frameCount)

            var totalFrames: Long = 0
            var underrunCount = 0

            while (isAudioRoutingActive.get()) {
                val vol = masterVolume * masterGain

                if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
                    // Native FluidSynth stereo synthesis
                    FluidSynthBindings.fluid_synth_write_s16(
                        fluidSynthPtr,
                        frameCount,
                        leftBuf, 0, 1,
                        rightBuf, 0, 1
                    )

                    // Interleave and apply master gain
                    var i = 0
                    var sampleIdx = 0
                    while (i < frameCount) {
                        val leftSample = (leftBuf[i] * vol).toInt().coerceIn(-32768, 32767)
                        val rightSample = (rightBuf[i] * vol).toInt().coerceIn(-32768, 32767)
                        shortBuffer[sampleIdx++] = leftSample.toShort()
                        shortBuffer[sampleIdx++] = rightSample.toShort()
                        i++
                    }
                } else {
                    // Fallback synthesis audio rendering
                    fallbackRenderer.renderStereoShorts(shortBuffer, frameCount, vol)
                }

                // Route audio buffer directly to Android system AudioTrack
                val written = track.write(shortBuffer, 0, shortBuffer.size)
                if (written < shortBuffer.size) {
                    underrunCount++
                }

                totalFrames += frameCount

                // Update diagnostic stats every ~200 buffers
                if (totalFrames % (frameCount * 200) == 0L) {
                    val latencyMs = (frameCount.toFloat() / sampleRate) * 1000f
                    _routingStats.value = AudioRoutingStats(
                        sampleRate = sampleRate,
                        bufferFrames = frameCount,
                        latencyMs = latencyMs,
                        totalFramesRouted = totalFrames,
                        underruns = underrunCount,
                        isNativeBackend = isNativeFluidSynthLoaded,
                        activeVoices = if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
                            FluidSynthBindings.fluid_synth_get_active_voice_count(fluidSynthPtr)
                        } else {
                            fallbackRenderer.getActiveVoiceCount()
                        }
                    )
                }
            }

            Log.i(TAG, "Audio routing pump terminated gracefully.")
        }, "FluidSynthAudioRoutingPump").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }

        _engineState.value = State.RUNNING
        return true
    }

    /**
     * Pauses or stops audio routing without disposing resources.
     */
    @Synchronized
    fun stopAudioRouting() {
        if (!isAudioRoutingActive.get()) return

        isAudioRoutingActive.set(false)
        try {
            audioRenderThread?.join(500)
        } catch (ignored: InterruptedException) {}
        audioRenderThread = null

        audioTrack?.pause()
        audioTrack?.flush()

        allNotesOff()
        _engineState.value = State.STOPPED
        Log.i(TAG, "AudioEngine audio routing stopped.")
    }

    // ==========================================
    // Real-Time MIDI & Worship Controls
    // ==========================================

    /**
     * Triggers a MIDI Note On event with specified velocity (0..127).
     */
    fun noteOn(channel: Int = 0, key: Int, velocity: Int = 100) {
        val clampedKey = key.coerceIn(0, 127)
        val clampedVel = velocity.coerceIn(0, 127)

        if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
            FluidSynthBindings.fluid_synth_noteon(fluidSynthPtr, channel, clampedKey, clampedVel)
        } else {
            fallbackRenderer.noteOn(channel, clampedKey, clampedVel)
        }
    }

    /**
     * Triggers a MIDI Note Off event.
     */
    fun noteOff(channel: Int = 0, key: Int) {
        val clampedKey = key.coerceIn(0, 127)

        if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
            FluidSynthBindings.fluid_synth_noteoff(fluidSynthPtr, channel, clampedKey)
        } else {
            fallbackRenderer.noteOff(channel, clampedKey)
        }
    }

    /**
     * Halts all active polyphonic voices across channels (panic button).
     */
    fun allNotesOff(channel: Int = -1) {
        if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
            if (channel == -1) {
                for (c in 0 until MIDI_CHANNELS) {
                    FluidSynthBindings.fluid_synth_all_notes_off(fluidSynthPtr, c)
                }
            } else {
                FluidSynthBindings.fluid_synth_all_notes_off(fluidSynthPtr, channel)
            }
        } else {
            fallbackRenderer.allNotesOff(channel)
        }
    }

    /**
     * Sends Pitch Bend command.
     * @param bendValue Normalized bend (-1.0 to +1.0) or MIDI range (0..16383, 8192 is center).
     */
    fun pitchBend(channel: Int = 0, bendNormalized: Float) {
        val clamped = bendNormalized.coerceIn(-1.0f, 1.0f)
        val midiBend = (8192 + (clamped * 8191)).toInt().coerceIn(0, 16383)

        if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
            FluidSynthBindings.fluid_synth_pitch_bend(fluidSynthPtr, channel, midiBend)
        } else {
            fallbackRenderer.setPitchBend(channel, clamped)
        }
    }

    /**
     * Sends a Control Change (CC) message to FluidSynth.
     */
    fun controlChange(channel: Int = 0, controller: Int, value: Int) {
        val clampedVal = value.coerceIn(0, 127)

        if (isNativeFluidSynthLoaded && fluidSynthPtr != 0L) {
            FluidSynthBindings.fluid_synth_cc(fluidSynthPtr, channel, controller, clampedVal)
        } else {
            fallbackRenderer.controlChange(channel, controller, clampedVal)
        }
    }

    /**
     * Adjusts Modulation Wheel (CC 1).
     * @param modNormalized Value between 0.0f and 1.0f.
     */
    fun setModulationWheel(channel: Int = 0, modNormalized: Float) {
        val midiVal = (modNormalized.coerceIn(0.0f, 1.0f) * 127).toInt()
        controlChange(channel, 1, midiVal)
    }

    /**
     * Adjusts Sustain Pedal (CC 64).
     */
    fun setSustainPedal(channel: Int = 0, sustainActive: Boolean) {
        controlChange(channel, 64, if (sustainActive) 127 else 0)
    }

    /**
     * Adjusts FluidSynth Reverb send depth (CC 91).
     */
    fun setReverbSend(channel: Int = 0, depthNormalized: Float) {
        val midiVal = (depthNormalized.coerceIn(0.0f, 1.0f) * 127).toInt()
        controlChange(channel, 91, midiVal)
    }

    /**
     * Adjusts FluidSynth Chorus send depth (CC 93).
     */
    fun setChorusSend(channel: Int = 0, depthNormalized: Float) {
        val midiVal = (depthNormalized.coerceIn(0.0f, 1.0f) * 127).toInt()
        controlChange(channel, 93, midiVal)
    }

    private fun updateStats() {
        val latencyMs = (bufferFrames.toFloat() / sampleRate) * 1000f
        _routingStats.value = AudioRoutingStats(
            sampleRate = sampleRate,
            bufferFrames = bufferFrames,
            latencyMs = latencyMs,
            totalFramesRouted = 0L,
            underruns = 0,
            isNativeBackend = isNativeFluidSynthLoaded,
            activeVoices = 0
        )
    }

    // ==========================================
    // Teardown & Resource Disposal
    // ==========================================

    /**
     * Completely releases AudioTrack, FluidSynth native resources, and frees memory.
     */
    @Synchronized
    fun release() {
        stopAudioRouting()

        audioTrack?.release()
        audioTrack = null

        soundFontsMap.clear()
        _loadedSoundFonts.value = emptyMap()
        _activeSoundFontId.value = null

        if (isNativeFluidSynthLoaded) {
            if (fluidSynthPtr != 0L) {
                FluidSynthBindings.delete_fluid_synth(fluidSynthPtr)
                fluidSynthPtr = 0L
            }
            if (fluidSettingsPtr != 0L) {
                FluidSynthBindings.delete_fluid_settings(fluidSettingsPtr)
                fluidSettingsPtr = 0L
            }
        }

        fallbackRenderer.release()
        _engineState.value = State.RELEASED
        Log.i(TAG, "AudioEngine and all associated resources successfully released.")
    }

    // ==========================================
    // Native JNI Interface Declarations
    // ==========================================

    /**
     * Standard C FluidSynth 2.x interface bindings.
     */
    private object FluidSynthBindings {
        external fun new_fluid_settings(): Long
        external fun fluid_settings_setnum(settings: Long, name: String, value: Double): Int
        external fun fluid_settings_setint(settings: Long, name: String, value: Int): Int
        external fun fluid_settings_setstr(settings: Long, name: String, value: String): Int
        external fun delete_fluid_settings(settings: Long)

        external fun new_fluid_synth(settings: Long): Long
        external fun delete_fluid_synth(synth: Long)

        external fun fluid_synth_sfload(synth: Long, filename: String, resetPresets: Int): Int
        external fun fluid_synth_sfunload(synth: Long, sfid: Int, resetPresets: Int): Int
        external fun fluid_synth_program_select(synth: Long, chan: Int, sfid: Int, bank: Int, prog: Int): Int

        external fun fluid_synth_noteon(synth: Long, chan: Int, key: Int, vel: Int): Int
        external fun fluid_synth_noteoff(synth: Long, chan: Int, key: Int): Int
        external fun fluid_synth_all_notes_off(synth: Long, chan: Int): Int
        external fun fluid_synth_pitch_bend(synth: Long, chan: Int, bend: Int): Int
        external fun fluid_synth_cc(synth: Long, chan: Int, num: Int, valNum: Int): Int

        external fun fluid_synth_write_s16(
            synth: Long,
            len: Int,
            lout: ShortArray, loff: Int, lincr: Int,
            rout: ShortArray, roff: Int, rincr: Int
        ): Int

        external fun fluid_synth_get_active_voice_count(synth: Long): Int
    }

    // ==========================================
    // Resilient Fallback Synthesizer
    // ==========================================

    /**
     * Autonomous fallback DSP synthesizer mimicking SoundFont polyphonic behavior.
     * Prevents crashes on systems where libfluidsynth.so is omitted.
     */
    private class FallbackFluidSynthRenderer {
        private var sampleRate: Int = 44100
        private var polyphony: Int = 64

        private class Voice {
            var active: Boolean = false
            var channel: Int = 0
            var midiNote: Int = 60
            var velocity: Float = 0f
            var phase1: Double = 0.0
            var phase2: Double = 0.0
            var envelope: Float = 0f
            var isReleasing: Boolean = false
        }

        private val voices = Array(64) { Voice() }
        private var pitchBendSemitones: Float = 0f
        private var modWheelDepth: Float = 0f

        fun initialize(sr: Int, poly: Int) {
            sampleRate = sr
            polyphony = poly.coerceIn(8, 64)
        }

        fun registerSoundFont(id: Int, file: File, presets: List<SoundFontPresetInfo>) {
            Log.d(TAG, "Fallback engine registered SoundFont $id with ${presets.size} presets.")
        }

        fun unregisterSoundFont(id: Int) {
            allNotesOff()
        }

        fun selectPreset(channel: Int, soundFontId: Int, bank: Int, program: Int) {
            Log.d(TAG, "Fallback preset chosen: chan=$channel, sf=$soundFontId, bank=$bank, prog=$program")
        }

        fun noteOn(channel: Int, note: Int, velocity: Int) {
            synchronized(voices) {
                // Find unused voice or steal oldest
                var target = voices.firstOrNull { !it.active }
                if (target == null) {
                    target = voices.minByOrNull { it.envelope } ?: voices[0]
                }
                target.active = true
                target.channel = channel
                target.midiNote = note
                target.velocity = velocity / 127f
                target.envelope = 0.05f
                target.isReleasing = false
                target.phase1 = 0.0
                target.phase2 = 0.0
            }
        }

        fun noteOff(channel: Int, note: Int) {
            synchronized(voices) {
                for (v in voices) {
                    if (v.active && v.channel == channel && v.midiNote == note) {
                        v.isReleasing = true
                    }
                }
            }
        }

        fun allNotesOff(channel: Int = -1) {
            synchronized(voices) {
                for (v in voices) {
                    if (channel == -1 || v.channel == channel) {
                        v.isReleasing = true
                    }
                }
            }
        }

        fun setPitchBend(channel: Int, bendNormalized: Float) {
            pitchBendSemitones = bendNormalized * 2.0f
        }

        fun controlChange(channel: Int, cc: Int, value: Int) {
            if (cc == 1) {
                modWheelDepth = value / 127f
            }
        }

        fun getActiveVoiceCount(): Int {
            return voices.count { it.active }
        }

        fun renderStereoShorts(output: ShortArray, frameCount: Int, gain: Float) {
            val sr = sampleRate.toDouble()
            var outIdx = 0

            synchronized(voices) {
                for (i in 0 until frameCount) {
                    var leftAcc = 0.0
                    var rightAcc = 0.0

                    for (v in voices) {
                        if (!v.active) continue

                        // ADSR Envelope calculation
                        if (v.isReleasing) {
                            v.envelope -= 0.0003f
                            if (v.envelope <= 0f) {
                                v.active = false
                                v.envelope = 0f
                                continue
                            }
                        } else if (v.envelope < 1.0f) {
                            v.envelope = min(1.0f, v.envelope + 0.001f)
                        }

                        val freq = 440.0 * Math.pow(2.0, (v.midiNote - 69.0 + pitchBendSemitones) / 12.0)
                        val inc1 = (2.0 * PI * freq) / sr
                        val inc2 = (2.0 * PI * (freq * 1.002)) / sr // Soft celestial detuning

                        v.phase1 += inc1
                        v.phase2 += inc2
                        if (v.phase1 > 2.0 * PI) v.phase1 -= 2.0 * PI
                        if (v.phase2 > 2.0 * PI) v.phase2 -= 2.0 * PI

                        val s1 = sin(v.phase1)
                        val s2 = cos(v.phase2)
                        val sig = (s1 * 0.6 + s2 * 0.4) * v.velocity * v.envelope

                        leftAcc += sig * 0.7
                        rightAcc += sig * 0.7
                    }

                    val leftClamped = (leftAcc * gain * 32767.0).toInt().coerceIn(-32768, 32767)
                    val rightClamped = (rightAcc * gain * 32767.0).toInt().coerceIn(-32768, 32767)

                    output[outIdx++] = leftClamped.toShort()
                    output[outIdx++] = rightClamped.toShort()
                }
            }
        }

        fun release() {
            allNotesOff()
        }
    }
}
