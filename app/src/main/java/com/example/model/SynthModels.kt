package com.example.model

enum class SoundCategory(val displayName: String, val description: String) {
    PADS("Pads", "Bancos de pad generosos, sus, padhi, dark e bright pads"),
    AMBIENT("Ambient", "Texturas, drones, vento, chuva e ambiências espaciais"),
    STRINGS("Strings", "Cordas orquestrais, cinematográficas, etéreas e bowed"),
    PROGRESSIVE("Progressive", "Synths progressivos, arpejos, leads suaves e dinâmicos"),
    WORSHIP_FX("Worship FX", "Swells, shimmer drops, sub-graves e transições"),
    CHORAL("Choral Pads", "Vozes celestiais, coro worship, formantes vocais e santuário")
}

enum class Tonality(val label: String) {
    MAJOR("MAJOR"),
    MINOR("MINOR")
}

enum class MinorScaleType(val label: String, val description: String) {
    HARMONIC_MINOR("Menor Harmônica", "7ª maior elevada (sensível dramática de louvor)"),
    MELODIC_MINOR("Menor Melódica", "6ª e 7ª maiores ascendentes (fluidez orquestral)"),
    NATURAL_MINOR("Menor Natural", "Escala menor clássica e intimista")
}

enum class RootNote(val symbol: String, val midiOffset: Int) {
    C("C", 0),
    C_SHARP("C#", 1),
    D("D", 2),
    D_SHARP("D#", 3),
    E("E", 4),
    F("F", 5),
    F_SHARP("F#", 6),
    G("G", 7),
    G_SHARP("G#", 8),
    A("A", 9),
    A_SHARP("A#", 10),
    B("B", 11)
}

enum class ArpPattern(val label: String) {
    UP("Up (Ascendente)"),
    DOWN("Down (Descendente)"),
    UP_DOWN("Up / Down (Ondular)"),
    WORSHIP_FLOAT("Worship Float (Aleatório Suave)")
}

enum class LfoDestination(val label: String) {
    TREMOLO("Tremolo (Volume)"),
    VIBRATO("Vibrato (Afinação)"),
    WAH("Wah (Filtro Cutoff)")
}

enum class FilterType(val label: String) {
    LOW_PASS("Low Pass (LP)"),
    HIGH_PASS("High Pass (HP)"),
    BAND_PASS("Band Pass (BP)")
}

enum class OscWaveform(val label: String) {
    WARM_SAW("Warm Sawtooth (Analógico)"),
    SUPER_SAW("Dual Supersaw (Encorpado)"),
    ORGAN_SINE("Pure Sine & Sub (Profundo)"),
    CHOIR_FORMANT("Vocal Choir (Formantes)"),
    ATMOS_NOISE("Ambient Rain & Wind (Ruído Orgânico)"),
    BELL_SHIMMER("Crystal Shimmer (Cintilante)")
}

data class Preset(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val category: SoundCategory,
    val bankIndex: Int,
    val waveform: OscWaveform = OscWaveform.WARM_SAW,
    val cutoffHz: Float = 3500f,
    val resonance: Float = 1.2f,
    val filterType: FilterType = FilterType.LOW_PASS,
    val attackSec: Float = 1.2f,
    val releaseSec: Float = 2.5f,
    val shimmerLevel: Float = 0.5f,
    val reverbWet: Float = 0.65f,
    val reverbDecay: Float = 0.8f,
    val delayTimeMs: Float = 380f,
    val delayFeedback: Float = 0.45f,
    val chorusDepth: Float = 0.5f,
    val arpeggiatorSpeedSec: Float = 2.0f,
    val isArpEnabled: Boolean = false,
    val lfoRateHz: Float = 1.0f,
    val lfoDepth: Float = 0.25f,
    val lfoDest: LfoDestination = LfoDestination.WAH,
    val isFavorite: Boolean = false
)

enum class WorshipVoicing(val label: String, val intervals: List<Int>) {
    FUNDAMENTAL("Fundamental + 5ª", listOf(0, 7, 12)),
    SUS2_AMBIENT("Sus2 (Worship Padrão)", listOf(0, 2, 7, 12, 14)),
    SUS4("Sus4 (Tensão e Resolução)", listOf(0, 5, 7, 12)),
    ADD9("Add9 (Brilho Celestial)", listOf(0, 4, 7, 14)),
    POWER_5("Power 1-5-8 (Firmeza)", listOf(0, 7, 12, 19)),
    SEVENTH("7ª Maj / Min", listOf(0, 4, 7, 11))
}
