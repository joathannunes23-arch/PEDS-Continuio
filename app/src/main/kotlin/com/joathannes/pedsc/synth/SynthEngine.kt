package com.joathannes.pedsc.synth

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class SoundPreset(
    val id: String,
    val name: String,
    val section: String,
    val file: String,
    val volume: Float,
    val arpeggio: Float,
    val lfoRate: Float,
    val lfoDepth: Float
)

class SynthEngine(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null
    private val soundPresets = mutableListOf<SoundPreset>()
    private var currentSound: SoundPreset? = null

    fun init() {
        loadSounds()
        initPlayer()
        playSound("pad-luxo") // inicia com Pad Luxo
    }

    private fun loadSounds() {
        val jsonString = context.assets.open("sounds.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<SoundPreset>>() {}.type
        soundPresets.addAll(Gson().fromJson(jsonString, listType))
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(context).build()
    }

    fun playSound(id: String) {
        currentSound = soundPresets.find { it.id == id }
        currentSound?.let { sound ->
            val sfFile = File(context.filesDir, sound.file)
            // Aqui você pode copiar os .sf2 para assets/raw ou usar o caminho certo
            // Por enquanto usa MediaItem para teste (funciona em todos os dispositivos)
            val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/raw/${sound.file.replace(".sf2", "")}")
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        }
    }

    fun release() {
        exoPlayer?.release()
    }
}
