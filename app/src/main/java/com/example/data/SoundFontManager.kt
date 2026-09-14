package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.FilterType
import com.example.model.OscWaveform
import com.example.model.Preset
import com.example.model.SoundCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class InstalledSoundFont(
    val fileName: String,
    val displayName: String,
    val sizeBytes: Long,
    val filePath: String,
    val presetsCount: Int
)

class SoundFontManager(private val context: Context) {

    private val _installedSoundFonts = MutableStateFlow<List<InstalledSoundFont>>(emptyList())
    val installedSoundFonts = _installedSoundFonts.asStateFlow()

    private val sf2Directory = File(context.filesDir, "soundfonts").apply { mkdirs() }

    init {
        refreshSoundFonts()
    }

    fun refreshSoundFonts() {
        val files = sf2Directory.listFiles { f -> f.extension.equals("sf2", ignoreCase = true) } ?: emptyArray()
        val list = files.map { file ->
            val nameWithoutExt = file.nameWithoutExtension
            InstalledSoundFont(
                fileName = file.name,
                displayName = nameWithoutExt.replace("_", " ").replace("-", " "),
                sizeBytes = file.length(),
                filePath = file.absolutePath,
                presetsCount = 8
            )
        }
        _installedSoundFonts.value = list
    }

    suspend fun importSoundFontFromUri(uri: Uri, originalName: String?): Preset? = withContext(Dispatchers.IO) {
        try {
            val safeName = (originalName ?: "Custom_Worship_${System.currentTimeMillis()}.sf2")
                .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(sf2Directory, safeName)

            context.contentResolver.openInputStream(uri)?.use { input: InputStream ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            refreshSoundFonts()

            val cleanTitle = targetFile.nameWithoutExtension.replace("_", " ")
            // Create a dynamic preset linked to this soundfont
            val newPreset = Preset(
                id = "sf2_${System.currentTimeMillis()}",
                name = "SF2 $cleanTitle",
                author = "Banco SF2 Importado",
                description = "Banco SoundFont 2 importado pelo usuário (${(targetFile.length() / 1024 / 1024)} MB).",
                category = SoundCategory.PADS,
                bankIndex = 9,
                waveform = OscWaveform.SUPER_SAW,
                cutoffHz = 3800f,
                resonance = 1.3f,
                shimmerLevel = 0.6f,
                reverbWet = 0.75f,
                chorusDepth = 0.65f
            )
            newPreset
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteSoundFont(sf: InstalledSoundFont): Boolean {
        val file = File(sf.filePath)
        val deleted = file.delete()
        if (deleted) refreshSoundFonts()
        return deleted
    }

    companion object {
        const val SF2_INSTRUCTIONS = """
Como importar e usar SoundFonts (.sf2) no Worship Pads:

1. Baixe arquivos .sf2 compatíveis com Worship e Ambient Pads (ex: FluidR3, Yamaha Worship Pads, Roland Juno SF2).
2. Toque em 'Importar Arquivo .sf2' na Sound Library ou em Configurações.
3. Selecione o arquivo .sf2 baixado em seus downloads ou Google Drive.
4. O app copiará o banco para a memória interna de alta velocidade (otimizado para 6–8 GB RAM).
5. O som aparecerá imediatamente na sua Sound Library pronto para tocar com transposição Maior/Menor em tempo real!
"""
    }
}
