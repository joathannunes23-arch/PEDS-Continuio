package com.example.data

import com.example.model.ArpPattern
import com.example.model.FilterType
import com.example.model.LfoDestination
import com.example.model.OscWaveform
import com.example.model.Preset
import com.example.model.SoundCategory

object PresetLibrary {

    val allPresets: List<Preset> = listOf(
        // =======================
        // SECTION 1: PADS (8 bancos)
        // =======================
        Preset(
            id = "pad_01",
            name = "Pad Luxo",
            author = "Worship Lab Brasil",
            description = "Textura densa e rica com harmônicos aveludados para momentos profundos de adoração.",
            category = SoundCategory.PADS,
            bankIndex = 1,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 3200f,
            resonance = 1.3f,
            shimmerLevel = 0.55f,
            reverbWet = 0.7f,
            reverbDecay = 0.85f,
            chorusDepth = 0.6f
        ),
        Preset(
            id = "pad_02",
            name = "Pad Worship 2025",
            author = "Tecladistas SP",
            description = "O som moderno de louvor congregacional com presença e sub-graves quentes e envolventes.",
            category = SoundCategory.PADS,
            bankIndex = 2,
            waveform = OscWaveform.WARM_SAW,
            cutoffHz = 4200f,
            resonance = 1.1f,
            shimmerLevel = 0.65f,
            reverbWet = 0.75f,
            delayTimeMs = 420f,
            delayFeedback = 0.5f
        ),
        Preset(
            id = "pad_03",
            name = "Pad Sustentado Celestial",
            author = "Aliança Praise",
            description = "Sustain infinito com ar analógico quente inspirado no Prophet 5 e transições suaves.",
            category = SoundCategory.PADS,
            bankIndex = 3,
            waveform = OscWaveform.WARM_SAW,
            cutoffHz = 2800f,
            resonance = 1.0f,
            attackSec = 2.0f,
            releaseSec = 3.5f,
            shimmerLevel = 0.4f,
            reverbWet = 0.8f
        ),
        Preset(
            id = "pad_04",
            name = "Dark Pad Intimista",
            author = "Sala do Trono",
            description = "Filtro fechado e aveludado ideal para orações congregacionais e fundo pastoral.",
            category = SoundCategory.PADS,
            bankIndex = 4,
            waveform = OscWaveform.ORGAN_SINE,
            cutoffHz = 1600f,
            resonance = 0.9f,
            shimmerLevel = 0.2f,
            reverbWet = 0.65f,
            chorusDepth = 0.4f
        ),
        Preset(
            id = "pad_05",
            name = "Bright Shimmer Pad",
            author = "Glória Synths",
            description = "Abertura cristalina com oitava superior brilhante e reverberação cintilante.",
            category = SoundCategory.PADS,
            bankIndex = 5,
            waveform = OscWaveform.BELL_SHIMMER,
            cutoffHz = 6500f,
            resonance = 1.5f,
            shimmerLevel = 0.85f,
            reverbWet = 0.8f,
            delayTimeMs = 380f
        ),
        Preset(
            id = "pad_06",
            name = "Pad Hi-Air Velvet",
            author = "Som & Vida Estúdio",
            description = "Camada de ar nas frequências altas que traz respiração à mixagem sem embolar os instrumentos.",
            category = SoundCategory.PADS,
            bankIndex = 6,
            waveform = OscWaveform.ATMOS_NOISE,
            cutoffHz = 4800f,
            filterType = FilterType.BAND_PASS,
            shimmerLevel = 0.5f,
            reverbWet = 0.7f
        ),
        Preset(
            id = "pad_07",
            name = "Pad Analógico Vintage",
            author = "Ministério Adoração",
            description = "Calor e modulação clássica de chorus estéreo inspirada no lendário Roland Juno-60.",
            category = SoundCategory.PADS,
            bankIndex = 7,
            waveform = OscWaveform.WARM_SAW,
            cutoffHz = 3400f,
            chorusDepth = 0.85f,
            reverbWet = 0.6f,
            delayFeedback = 0.4f
        ),
        Preset(
            id = "pad_08",
            name = "Pad Atmosfera Profunda",
            author = "Betel Sounds",
            description = "Graves macios e sustentação orquestrada para transições entre canções de louvor.",
            category = SoundCategory.PADS,
            bankIndex = 8,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 2400f,
            resonance = 1.2f,
            shimmerLevel = 0.45f,
            reverbWet = 0.75f
        ),

        // =======================
        // SECTION 2: AMBIENT (8 bancos)
        // =======================
        Preset(
            id = "amb_01",
            name = "Ambient Éter",
            author = "Éter Soundscapes",
            description = "Textura contínua de amplidão sideral com modulação de fase e sensação de infinito.",
            category = SoundCategory.AMBIENT,
            bankIndex = 1,
            waveform = OscWaveform.ORGAN_SINE,
            cutoffHz = 2900f,
            lfoRateHz = 0.4f,
            lfoDepth = 0.35f,
            lfoDest = LfoDestination.WAH,
            reverbWet = 0.85f
        ),
        Preset(
            id = "amb_02",
            name = "Drone Santo",
            author = "Altar Drones",
            description = "Sub-grave de alicerce harmônico estático com harmônicos ressonantes meditativos.",
            category = SoundCategory.AMBIENT,
            bankIndex = 2,
            waveform = OscWaveform.ORGAN_SINE,
            cutoffHz = 1800f,
            resonance = 1.6f,
            shimmerLevel = 0.3f,
            reverbWet = 0.7f
        ),
        Preset(
            id = "amb_03",
            name = "Chuva da Graça",
            author = "Ambiente & Paz",
            description = "Ruído rosa delicadamente filtrado emulando gotas e chuva suave sob pad musical.",
            category = SoundCategory.AMBIENT,
            bankIndex = 3,
            waveform = OscWaveform.ATMOS_NOISE,
            cutoffHz = 3800f,
            shimmerLevel = 0.35f,
            reverbWet = 0.8f,
            delayTimeMs = 480f
        ),
        Preset(
            id = "amb_04",
            name = "Vento Suave",
            author = "Vento do Espírito",
            description = "Filtro passa-faixa dinâmico com modulação orgânica contínua como brisa da manhã.",
            category = SoundCategory.AMBIENT,
            bankIndex = 4,
            waveform = OscWaveform.ATMOS_NOISE,
            filterType = FilterType.BAND_PASS,
            cutoffHz = 2200f,
            resonance = 2.0f,
            lfoRateHz = 0.25f,
            lfoDepth = 0.5f,
            reverbWet = 0.75f
        ),
        Preset(
            id = "amb_05",
            name = "Espaço Sagrado",
            author = "Santuário Sounds",
            description = "Acústica imersiva de catedral com difusão extrema e ressonância etérea puríssima.",
            category = SoundCategory.AMBIENT,
            bankIndex = 5,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 3100f,
            reverbWet = 0.9f,
            reverbDecay = 0.95f,
            shimmerLevel = 0.6f
        ),
        Preset(
            id = "amb_06",
            name = "Textura Mística",
            author = "Harmonia Pura",
            description = "Modulação suave de 360 graus com ambiência circular envolvente.",
            category = SoundCategory.AMBIENT,
            bankIndex = 6,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 2700f,
            chorusDepth = 0.8f,
            reverbWet = 0.7f
        ),
        Preset(
            id = "amb_07",
            name = "Aurora Boreal Worship",
            author = "Luz Celeste",
            description = "Brilhos e cintilações harmônicas que surgem e desaparecem em ondas místicas.",
            category = SoundCategory.AMBIENT,
            bankIndex = 7,
            waveform = OscWaveform.BELL_SHIMMER,
            cutoffHz = 5200f,
            shimmerLevel = 0.75f,
            delayTimeMs = 520f,
            delayFeedback = 0.55f
        ),
        Preset(
            id = "amb_08",
            name = "Abismo de Glória",
            author = "Profundeza Eterna",
            description = "Graves aveludados com saturação harmônica sutil simulando pré-amplificador a válvula.",
            category = SoundCategory.AMBIENT,
            bankIndex = 8,
            waveform = OscWaveform.WARM_SAW,
            cutoffHz = 1900f,
            resonance = 1.4f,
            reverbWet = 0.65f
        ),

        // =======================
        // SECTION 3: STRINGS (8 bancos)
        // =======================
        Preset(
            id = "str_01",
            name = "String Coração",
            author = "Cordas Vivas",
            description = "Ensemble orquestral caloroso com ataque suave e expressividade pastoral tocante.",
            category = SoundCategory.STRINGS,
            bankIndex = 1,
            waveform = OscWaveform.SUPER_SAW,
            attackSec = 1.6f,
            releaseSec = 2.8f,
            cutoffHz = 4100f,
            chorusDepth = 0.7f,
            reverbWet = 0.7f
        ),
        Preset(
            id = "str_02",
            name = "Orquestra Celeste",
            author = "Filarmônica Worship",
            description = "Violinos e cellos cinematográficos grandiosos para clímax congregacionais poderosos.",
            category = SoundCategory.STRINGS,
            bankIndex = 2,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 5000f,
            shimmerLevel = 0.5f,
            reverbWet = 0.8f,
            chorusDepth = 0.65f
        ),
        Preset(
            id = "str_03",
            name = "Cinematic Bowed",
            author = "Trilha Sacra",
            description = "Cordas friccionadas com arco lento e peso orquestral autêntico na fundamental.",
            category = SoundCategory.STRINGS,
            bankIndex = 3,
            waveform = OscWaveform.WARM_SAW,
            attackSec = 2.2f,
            cutoffHz = 3600f,
            resonance = 1.1f,
            reverbWet = 0.75f
        ),
        Preset(
            id = "str_04",
            name = "Ethereal Violins",
            author = "Harpas & Cordas",
            description = "Sexteto de violinos nas frequências altas com shimmer de oitava e arpejos suaves.",
            category = SoundCategory.STRINGS,
            bankIndex = 4,
            waveform = OscWaveform.BELL_SHIMMER,
            cutoffHz = 5800f,
            shimmerLevel = 0.7f,
            delayTimeMs = 360f,
            reverbWet = 0.75f
        ),
        Preset(
            id = "str_05",
            name = "Cello do Louvor",
            author = "Maestro Daniel",
            description = "Timbre encorpado nas notas graves com rica ressonância de madeira e calor acústico.",
            category = SoundCategory.STRINGS,
            bankIndex = 5,
            waveform = OscWaveform.WARM_SAW,
            cutoffHz = 2200f,
            resonance = 1.3f,
            reverbWet = 0.6f
        ),
        Preset(
            id = "str_06",
            name = "Cordas de Sião",
            author = "Louvor Israel",
            description = "Harmonia acústica e sintética com textura tradicional para momentos de comunhão.",
            category = SoundCategory.STRINGS,
            bankIndex = 6,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 3700f,
            chorusDepth = 0.5f,
            reverbWet = 0.7f
        ),
        Preset(
            id = "str_07",
            name = "Sinfonia da Cruz",
            author = "Orquestra Ágape",
            description = "Arranjo solene e grandioso para hinos clássicos com amplitude orquestral completa.",
            category = SoundCategory.STRINGS,
            bankIndex = 7,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 4600f,
            shimmerLevel = 0.4f,
            reverbWet = 0.8f
        ),
        Preset(
            id = "str_08",
            name = "Pizzicato & Pad",
            author = "Studio Renovo",
            description = "Ataque inicial com sustentação aveludada contínua para andamentos lentos.",
            category = SoundCategory.STRINGS,
            bankIndex = 8,
            waveform = OscWaveform.WARM_SAW,
            attackSec = 0.8f,
            cutoffHz = 3900f,
            delayTimeMs = 400f,
            delayFeedback = 0.5f,
            reverbWet = 0.7f
        ),

        // =======================
        // SECTION 4: PROGRESSIVE (8 bancos)
        // =======================
        Preset(
            id = "prg_01",
            name = "Progressive Motion",
            author = "Movimento Worship",
            description = "Pulsação moderna com arpejador lento de 2 segundos e abertura rítmica envolvente.",
            category = SoundCategory.PROGRESSIVE,
            bankIndex = 1,
            waveform = OscWaveform.SUPER_SAW,
            isArpEnabled = true,
            arpeggiatorSpeedSec = 2.0f,
            cutoffHz = 4400f,
            shimmerLevel = 0.5f,
            delayTimeMs = 380f,
            delayFeedback = 0.5f
        ),
        Preset(
            id = "prg_02",
            name = "Synth Elevação",
            author = "Avivamento Music",
            description = "Subida harmônica progressiva com ressonância de filtro que conduz a banda ao ápice.",
            category = SoundCategory.PROGRESSIVE,
            bankIndex = 2,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 4800f,
            resonance = 1.8f,
            lfoRateHz = 0.5f,
            lfoDepth = 0.4f,
            lfoDest = LfoDestination.WAH,
            reverbWet = 0.7f
        ),
        Preset(
            id = "prg_03",
            name = "Arpejo da Manhã",
            author = "Aurora Som",
            description = "Padrão arpejado sutil de 4 notas ascendentes com cauda longa de delay sincronizado.",
            category = SoundCategory.PROGRESSIVE,
            bankIndex = 3,
            waveform = OscWaveform.BELL_SHIMMER,
            isArpEnabled = true,
            arpeggiatorSpeedSec = 1.5f,
            cutoffHz = 5200f,
            delayTimeMs = 420f,
            reverbWet = 0.75f
        ),
        Preset(
            id = "prg_04",
            name = "Lead Suave Pentecostes",
            author = "Fogo Alto",
            description = "Timbre doce de sintetizador monofônico com glide aveludado e ambiência catedral.",
            category = SoundCategory.PROGRESSIVE,
            bankIndex = 4,
            waveform = OscWaveform.WARM_SAW,
            cutoffHz = 3600f,
            resonance = 1.5f,
            chorusDepth = 0.6f,
            reverbWet = 0.7f
        ),
        Preset(
            id = "prg_05",
            name = "Pad Pulsante 8va",
            author = "Dynamic Praise",
            description = "Oscilador duplo com salto sutil de oitava gerando pulso natural para músicas rápidas.",
            category = SoundCategory.PROGRESSIVE,
            bankIndex = 5,
            waveform = OscWaveform.SUPER_SAW,
            isArpEnabled = true,
            arpeggiatorSpeedSec = 1.0f,
            cutoffHz = 4200f,
            reverbWet = 0.65f
        ),
        Preset(
            id = "prg_06",
            name = "Serum Waves Worship",
            author = "Sound Designer Brasil",
            description = "Tabelas de onda com filtro de corte cirúrgico e presença moderna inspirada no Serum.",
            category = SoundCategory.PROGRESSIVE,
            bankIndex = 6,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 5600f,
            resonance = 2.0f,
            delayTimeMs = 350f,
            delayFeedback = 0.45f
        ),
        Preset(
            id = "prg_07",
            name = "Progressão Cósmica",
            author = "Galáxias do Criador",
            description = "Camadas crescentes com arpejador Worship Float aleatório e eco pontuado.",
            category = SoundCategory.PROGRESSIVE,
            bankIndex = 7,
            waveform = OscWaveform.BELL_SHIMMER,
            isArpEnabled = true,
            arpeggiatorSpeedSec = 2.5f,
            cutoffHz = 4900f,
            reverbWet = 0.8f
        ),
        Preset(
            id = "prg_08",
            name = "Blofeld Worship Tone",
            author = "Vintage Digital",
            description = "Síntese digital alemã com filtros multimodo e brilho inconfundível para bases amplas.",
            category = SoundCategory.PROGRESSIVE,
            bankIndex = 8,
            waveform = OscWaveform.WARM_SAW,
            cutoffHz = 4300f,
            resonance = 1.4f,
            chorusDepth = 0.75f,
            reverbWet = 0.75f
        ),

        // =======================
        // SECTION 5: WORSHIP FX (8 bancos)
        // =======================
        Preset(
            id = "wfx_01",
            name = "Swell da Glória",
            author = "Impacto Worship",
            description = "Crescendo lento de abertura de filtro com explosão de reverb para introduções solenes.",
            category = SoundCategory.WORSHIP_FX,
            bankIndex = 1,
            waveform = OscWaveform.SUPER_SAW,
            attackSec = 3.0f,
            releaseSec = 4.0f,
            cutoffHz = 5000f,
            reverbWet = 0.85f,
            shimmerLevel = 0.7f
        ),
        Preset(
            id = "wfx_02",
            name = "Shimmer Drop",
            author = "Queda Celeste",
            description = "Descida tonal etérea que relaxa o ambiente para o início da oração ou pregação.",
            category = SoundCategory.WORSHIP_FX,
            bankIndex = 2,
            waveform = OscWaveform.BELL_SHIMMER,
            cutoffHz = 3800f,
            shimmerLevel = 0.9f,
            delayTimeMs = 450f,
            delayFeedback = 0.6f
        ),
        Preset(
            id = "wfx_03",
            name = "Sub Bass Trovoada",
            author = "Fundamento Forte",
            description = "Onda senoidal pura em 35Hz para dar pressão e peso absoluto nas viradas da bateria.",
            category = SoundCategory.WORSHIP_FX,
            bankIndex = 3,
            waveform = OscWaveform.ORGAN_SINE,
            cutoffHz = 1200f,
            resonance = 0.8f,
            shimmerLevel = 0.0f,
            reverbWet = 0.4f
        ),
        Preset(
            id = "wfx_04",
            name = "Glissando Celestial",
            author = "Harpa Sagrada",
            description = "Deslize contínuo de afinação entre notas com cauda longa de ping-pong delay.",
            category = SoundCategory.WORSHIP_FX,
            bankIndex = 4,
            waveform = OscWaveform.BELL_SHIMMER,
            cutoffHz = 4800f,
            delayTimeMs = 400f,
            delayFeedback = 0.55f,
            reverbWet = 0.75f
        ),
        Preset(
            id = "wfx_05",
            name = "Transição de Fé",
            author = "Ponte da Esperança",
            description = "Camada harmônica híbrida projetada para transições perfeitas entre canções diferentes.",
            category = SoundCategory.WORSHIP_FX,
            bankIndex = 5,
            waveform = OscWaveform.WARM_SAW,
            cutoffHz = 3200f,
            attackSec = 2.0f,
            reverbWet = 0.8f
        ),
        Preset(
            id = "wfx_06",
            name = "Noise Shimmer Sweep",
            author = "Atmosfera Viva",
            description = "Varredura contínua de ar harmônico cintilante para momentos de ministração livre.",
            category = SoundCategory.WORSHIP_FX,
            bankIndex = 6,
            waveform = OscWaveform.ATMOS_NOISE,
            filterType = FilterType.BAND_PASS,
            cutoffHz = 4100f,
            resonance = 2.2f,
            reverbWet = 0.85f
        ),
        Preset(
            id = "wfx_07",
            name = "Sino do Santuário",
            author = "Badalo Real",
            description = "Harmônicos metálicos puros e ressonantes que trazem paz e reverência imediata.",
            category = SoundCategory.WORSHIP_FX,
            bankIndex = 7,
            waveform = OscWaveform.BELL_SHIMMER,
            cutoffHz = 6000f,
            shimmerLevel = 0.8f,
            reverbWet = 0.75f
        ),
        Preset(
            id = "wfx_08",
            name = "Subida de Louvor",
            author = "Clímax Worship",
            description = "Camada de sintetizador com ressonância crescente que constrói tensão espiritual.",
            category = SoundCategory.WORSHIP_FX,
            bankIndex = 8,
            waveform = OscWaveform.SUPER_SAW,
            cutoffHz = 4500f,
            resonance = 1.9f,
            reverbWet = 0.7f
        ),

        // =======================
        // SECTION 6: CHORAL PADS (8 bancos)
        // =======================
        Preset(
            id = "cho_01",
            name = "Vozes Celestiais",
            author = "Coro dos Anjos",
            description = "Formantes vocais 'Ah' e 'Oh' com calor humano e sustentação infinita de catedral.",
            category = SoundCategory.CHORAL,
            bankIndex = 1,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 3300f,
            shimmerLevel = 0.5f,
            reverbWet = 0.85f,
            chorusDepth = 0.7f
        ),
        Preset(
            id = "cho_02",
            name = "Choir Worship",
            author = "Santuário Vox",
            description = "Coro misto congregacional suave que acompanha a igreja com doçura e afinação pura.",
            category = SoundCategory.CHORAL,
            bankIndex = 2,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 3600f,
            reverbWet = 0.8f,
            chorusDepth = 0.6f
        ),
        Preset(
            id = "cho_03",
            name = "Sanctus Vocalis",
            author = "Coral Eclesiástico",
            description = "Textura coral solene e reverente para celebrações de Santa Ceia e quebrantamento.",
            category = SoundCategory.CHORAL,
            bankIndex = 3,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 2800f,
            resonance = 1.2f,
            reverbWet = 0.9f
        ),
        Preset(
            id = "cho_04",
            name = "Vocal Air Shimmer",
            author = "Brisa Sonora",
            description = "Vozes femininas etéreas com brilho de ar nas frequências altas e oitava cintilante.",
            category = SoundCategory.CHORAL,
            bankIndex = 4,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 5000f,
            shimmerLevel = 0.75f,
            reverbWet = 0.8f,
            delayTimeMs = 400f
        ),
        Preset(
            id = "cho_05",
            name = "Coral de Sião",
            author = "Jerusalém Choir",
            description = "Harmonia vocal encorpada com textura aveludada e sustentação perfeita para orações.",
            category = SoundCategory.CHORAL,
            bankIndex = 5,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 3100f,
            chorusDepth = 0.65f,
            reverbWet = 0.75f
        ),
        Preset(
            id = "cho_06",
            name = "Meninos Cantores",
            author = "Vozes da Paz",
            description = "Formantes infantis com pureza harmônica cristalina e ressonância etérea.",
            category = SoundCategory.CHORAL,
            bankIndex = 6,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 4200f,
            shimmerLevel = 0.6f,
            reverbWet = 0.85f
        ),
        Preset(
            id = "cho_07",
            name = "Cântico Novo",
            author = "Graça Vox",
            description = "Camada dinâmica de respiração e vozes sintéticas com modulação sutil de LFO.",
            category = SoundCategory.CHORAL,
            bankIndex = 7,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 3800f,
            lfoRateHz = 0.3f,
            lfoDepth = 0.3f,
            reverbWet = 0.8f
        ),
        Preset(
            id = "cho_08",
            name = "Voz do Trono",
            author = "Majestade Sounds",
            description = "Vozes polifônicas profundas com ressonância majestosa e sub-grave presente.",
            category = SoundCategory.CHORAL,
            bankIndex = 8,
            waveform = OscWaveform.CHOIR_FORMANT,
            cutoffHz = 3400f,
            resonance = 1.3f,
            shimmerLevel = 0.55f,
            reverbWet = 0.85f
        )
    )

    val quickPlayPresets: List<Preset> = listOf(
        allPresets[0], // Pad Luxo
        allPresets[1], // Pad Worship 2025
        allPresets[8], // Ambient Éter
        allPresets[16], // String Coração
        allPresets[40]  // Vozes Celestiais
    )

    fun getPresetsByCategory(category: SoundCategory): List<Preset> {
        return allPresets.filter { it.category == category }
    }
}
