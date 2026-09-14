package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.LooperState
import com.example.audio.PerformanceRecorder
import com.example.audio.WorshipLooper
import com.example.audio.WorshipMetronome
import com.example.audio.WorshipSynthEngine
import com.example.data.InstalledSoundFont
import com.example.data.PresetLibrary
import com.example.data.SoundFontManager
import com.example.model.ArpPattern
import com.example.model.FilterType
import com.example.model.LfoDestination
import com.example.model.MinorScaleType
import com.example.model.Preset
import com.example.model.RootNote
import com.example.model.SoundCategory
import com.example.model.Tonality
import com.example.model.WorshipVoicing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorshipViewModel(application: Application) : AndroidViewModel(application) {

    val synthEngine = WorshipSynthEngine()
    val performanceRecorder = PerformanceRecorder(application.applicationContext)
    val worshipLooper = WorshipLooper()
    val metronome = WorshipMetronome()
    val soundFontManager = SoundFontManager(application.applicationContext)

    // Current Playback State
    val isPlaying = MutableStateFlow(false)
    val activeRootNote = MutableStateFlow(RootNote.C)
    val tonality = MutableStateFlow(Tonality.MAJOR)
    val minorScaleType = MutableStateFlow(MinorScaleType.HARMONIC_MINOR)
    val worshipVoicing = MutableStateFlow(WorshipVoicing.SUS2_AMBIENT)

    // Active Preset
    val currentPreset = MutableStateFlow(PresetLibrary.quickPlayPresets[0])
    val customPresets = MutableStateFlow<List<Preset>>(emptyList())

    // Library Navigation State
    val selectedCategory = MutableStateFlow(SoundCategory.PADS)
    val searchQuery = MutableStateFlow("")

    // Synth Rack Parameters State
    val masterVolume = MutableStateFlow(0.85f)
    val octaveShift = MutableStateFlow(0)
    val pitchBend = MutableStateFlow(0.0f)
    val modWheel = MutableStateFlow(0.0f)
    val tuningBaseHz = MutableStateFlow(440f)
    val fineDetuneCents = MutableStateFlow(0f)
    val pan360 = MutableStateFlow(0.0f)

    // Filter & Modulation
    val cutoffHz = MutableStateFlow(3500f)
    val resonance = MutableStateFlow(1.2f)
    val filterType = MutableStateFlow(FilterType.LOW_PASS)

    val isArpEnabled = MutableStateFlow(false)
    val arpSpeedSec = MutableStateFlow(2.0f)
    val arpPattern = MutableStateFlow(ArpPattern.WORSHIP_FLOAT)

    val lfoRateHz = MutableStateFlow(1.0f)
    val lfoDepth = MutableStateFlow(0.25f)
    val lfoDest = MutableStateFlow(LfoDestination.WAH)

    // Effects
    val reverbWet = MutableStateFlow(0.65f)
    val reverbDecay = MutableStateFlow(0.8f)
    val shimmerLevel = MutableStateFlow(0.5f)
    val delayTimeMs = MutableStateFlow(380f)
    val delayFeedback = MutableStateFlow(0.45f)
    val chorusDepth = MutableStateFlow(0.5f)

    // Category Volume Faders
    val volumePads = MutableStateFlow(1.0f)
    val volumeAmbient = MutableStateFlow(1.0f)
    val volumeStrings = MutableStateFlow(1.0f)
    val volumeProgressive = MutableStateFlow(1.0f)
    val volumeWorshipFx = MutableStateFlow(1.0f)
    val volumeChoral = MutableStateFlow(1.0f)

    // Performance Mode (Fullscreen stage view)
    val isPerformanceMode = MutableStateFlow(false)

    // Visualizer data
    val visualizerWaveform = MutableStateFlow(FloatArray(64))
    val visualizerRms = MutableStateFlow(0f)

    // User Message Toast / Banner
    val statusNotification = MutableStateFlow<String?>(null)

    // Filtered Presets List combining built-in + imported custom presets
    val displayedPresets = combine(
        selectedCategory,
        searchQuery,
        customPresets
    ) { category, query, customs ->
        val fullList = PresetLibrary.allPresets + customs
        val inCategory = fullList.filter { it.category == category }
        if (query.isBlank()) {
            inCategory
        } else {
            inCategory.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PresetLibrary.getPresetsByCategory(SoundCategory.PADS))

    init {
        synthEngine.startEngine()
        applyPreset(currentPreset.value)

        // Connect synth engine to recorder & looper
        synthEngine.audioOutputListener = { buffer, frameCount ->
            performanceRecorder.onAudioSamples(buffer, frameCount)
            worshipLooper.processAudio(buffer, frameCount)
        }

        // Connect real-time visualizer with low-overhead downsampling
        val visBuffer = FloatArray(64)
        synthEngine.visualizerCallback = { waveSamples, rms ->
            if (rms > 0.001f || visualizerRms.value > 0.001f) {
                val step = (waveSamples.size / 64).coerceAtLeast(1)
                for (i in 0 until 64) {
                    val idx = i * step
                    visBuffer[i] = if (idx < waveSamples.size) waveSamples[idx] else 0f
                }
                visualizerWaveform.value = visBuffer.clone()
                visualizerRms.value = rms
            }
        }
    }

    fun togglePadPlay(note: RootNote? = null) {
        if (note != null && activeRootNote.value != note) {
            // Change root note and ensure play is on
            activeRootNote.value = note
            synthEngine.playNote(note)
            isPlaying.value = true
        } else if (note != null && activeRootNote.value == note) {
            // Tap active note again -> toggle play/stop
            val newState = !isPlaying.value
            isPlaying.value = newState
            if (newState) synthEngine.playNote(note) else synthEngine.stopPlay()
        } else {
            // Generic play/stop toggle
            val newState = !isPlaying.value
            isPlaying.value = newState
            if (newState) synthEngine.playNote(activeRootNote.value) else synthEngine.stopPlay()
        }
    }

    fun stopPad() {
        isPlaying.value = false
        synthEngine.stopPlay()
    }

    fun toggleTonality() {
        val next = if (tonality.value == Tonality.MAJOR) Tonality.MINOR else Tonality.MAJOR
        tonality.value = next
        synthEngine.setTonality(next)
    }

    fun setTonality(newTonality: Tonality) {
        tonality.value = newTonality
        synthEngine.setTonality(newTonality)
    }

    fun setMinorScaleType(type: MinorScaleType) {
        minorScaleType.value = type
        synthEngine.minorScaleType.set(type)
    }

    fun setWorshipVoicing(voicing: WorshipVoicing) {
        worshipVoicing.value = voicing
        synthEngine.worshipVoicing.set(voicing)
    }

    fun applyPreset(preset: Preset) {
        currentPreset.value = preset
        synthEngine.applyPreset(preset)

        cutoffHz.value = preset.cutoffHz
        resonance.value = preset.resonance
        filterType.value = preset.filterType
        shimmerLevel.value = preset.shimmerLevel
        reverbWet.value = preset.reverbWet
        reverbDecay.value = preset.reverbDecay
        delayTimeMs.value = preset.delayTimeMs
        delayFeedback.value = preset.delayFeedback
        chorusDepth.value = preset.chorusDepth
        isArpEnabled.value = preset.isArpEnabled
        arpSpeedSec.value = preset.arpeggiatorSpeedSec
        lfoRateHz.value = preset.lfoRateHz
        lfoDepth.value = preset.lfoDepth
        lfoDest.value = preset.lfoDest
    }

    fun setMasterVolume(v: Float) {
        masterVolume.value = v
        synthEngine.masterVolume = v
    }

    fun setOctaveShift(shift: Int) {
        val clamped = shift.coerceIn(-1, 1)
        octaveShift.value = clamped
        synthEngine.octaveShift = clamped
    }

    fun setPitchBend(pb: Float) {
        pitchBend.value = pb
        synthEngine.pitchBendSemitones = pb
    }

    fun setModWheel(mw: Float) {
        modWheel.value = mw
        synthEngine.modWheel = mw
    }

    fun setTuningBaseHz(hz: Float) {
        tuningBaseHz.value = hz
        synthEngine.tuningBaseHz = hz
    }

    fun setFineDetuneCents(cents: Float) {
        fineDetuneCents.value = cents
        synthEngine.fineDetuneCents = cents
    }

    fun setPan360(pan: Float) {
        pan360.value = pan
        synthEngine.pan360 = pan
    }

    fun setCutoff(hz: Float) {
        cutoffHz.value = hz
        synthEngine.cutoffHz = hz
    }

    fun setResonance(r: Float) {
        resonance.value = r
        synthEngine.resonance = r
    }

    fun setFilterType(type: FilterType) {
        filterType.value = type
        synthEngine.filterType = type
    }

    fun setArpEnabled(enabled: Boolean) {
        isArpEnabled.value = enabled
        synthEngine.isArpEnabled = enabled
    }

    fun setArpSpeed(sec: Float) {
        arpSpeedSec.value = sec
        synthEngine.arpSpeedSec = sec
    }

    fun setArpPattern(p: ArpPattern) {
        arpPattern.value = p
        synthEngine.arpPattern = p
    }

    fun setLfoRate(hz: Float) {
        lfoRateHz.value = hz
        synthEngine.lfoRateHz = hz
    }

    fun setLfoDepth(d: Float) {
        lfoDepth.value = d
        synthEngine.lfoDepth = d
    }

    fun setLfoDestination(dest: LfoDestination) {
        lfoDest.value = dest
        synthEngine.lfoDestination = dest
    }

    fun setReverbWet(wet: Float) {
        reverbWet.value = wet
        synthEngine.reverbWet = wet
    }

    fun setReverbDecay(decay: Float) {
        reverbDecay.value = decay
        synthEngine.reverbDecay = decay
    }

    fun setShimmerLevel(shimmer: Float) {
        shimmerLevel.value = shimmer
        synthEngine.shimmerLevel = shimmer
    }

    fun setDelayTime(timeMs: Float) {
        delayTimeMs.value = timeMs
        synthEngine.delayTimeMs = timeMs
    }

    fun setDelayFeedback(fb: Float) {
        delayFeedback.value = fb
        synthEngine.delayFeedback = fb
    }

    fun setChorusDepth(d: Float) {
        chorusDepth.value = d
        synthEngine.chorusDepth = d
    }

    fun setCategoryVolume(category: SoundCategory, vol: Float) {
        when (category) {
            SoundCategory.PADS -> {
                volumePads.value = vol
                synthEngine.volumePads = vol
            }
            SoundCategory.AMBIENT -> {
                volumeAmbient.value = vol
                synthEngine.volumeAmbient = vol
            }
            SoundCategory.STRINGS -> {
                volumeStrings.value = vol
                synthEngine.volumeStrings = vol
            }
            SoundCategory.PROGRESSIVE -> {
                volumeProgressive.value = vol
                synthEngine.volumeProgressive = vol
            }
            SoundCategory.WORSHIP_FX -> {
                volumeWorshipFx.value = vol
                synthEngine.volumeWorshipFx = vol
            }
            SoundCategory.CHORAL -> {
                volumeChoral.value = vol
                synthEngine.volumeChoral = vol
            }
        }
    }

    // Recorder Actions
    fun toggleRecording() {
        if (performanceRecorder.isRecording.value) {
            val path = performanceRecorder.stopRecording()
            if (path != null) {
                statusNotification.value = "Gravação WAV salva com sucesso!"
            }
        } else {
            val started = performanceRecorder.startRecording()
            if (started) {
                statusNotification.value = "Gravando performance ao vivo (WAV)..."
            }
        }
    }

    fun shareLastRecording(context: Context) {
        performanceRecorder.shareLastRecording(context)
    }

    // Looper Actions
    fun startLooperRecord() {
        worshipLooper.startRecord8Bars(metronome.bpm.value)
        statusNotification.value = "Gravando loop de 8 compassos..."
    }

    fun toggleLooperPlay() {
        worshipLooper.togglePlayStop()
    }

    fun clearLooper() {
        worshipLooper.clear()
        statusNotification.value = "Loop de 8 barras apagado."
    }

    // Metronome Actions
    fun toggleMetronome() {
        metronome.toggle()
    }

    fun setMetronomeBpm(bpm: Int) {
        metronome.setBpm(bpm)
    }

    fun tapMetronome() {
        metronome.tapTempo()
    }

    // SoundFont Import
    fun importSoundFont(uri: Uri, displayName: String?) {
        viewModelScope.launch {
            val importedPreset = soundFontManager.importSoundFontFromUri(uri, displayName)
            if (importedPreset != null) {
                customPresets.value = customPresets.value + importedPreset
                applyPreset(importedPreset)
                statusNotification.value = "Banco SoundFont '${importedPreset.name}' importado com sucesso!"
            } else {
                statusNotification.value = "Erro ao importar arquivo SoundFont .sf2."
            }
        }
    }

    fun clearStatusNotification() {
        statusNotification.value = null
    }

    override fun onCleared() {
        super.onCleared()
        synthEngine.stopEngine()
        metronome.release()
    }
}
