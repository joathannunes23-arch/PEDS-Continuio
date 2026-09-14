package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Worship Pads", appName)
  }

  @Test
  fun `preset library contains all required categories and presets`() {
    val allPresets = com.example.data.PresetLibrary.allPresets
    assertEquals(48, allPresets.size)
    assertEquals(6, com.example.model.SoundCategory.values().size)
  }

  @Test
  fun `root note midi offsets are correct`() {
    val aOffset = com.example.model.RootNote.A.midiOffset
    assertEquals(9, aOffset)
    val cOffset = com.example.model.RootNote.C.midiOffset
    assertEquals(0, cOffset)
  }

  @Test
  fun `audio engine initializes and manages lifecycle properly`() {
    val audioEngine = com.example.audio.AudioEngine.getInstance()
    val initialized = audioEngine.initialize(sampleRate = 44100, bufferFrames = 512, polyphony = 32)
    org.junit.Assert.assertTrue(initialized)
    assertEquals(com.example.audio.AudioEngine.State.READY, audioEngine.engineState.value)

    // Test routing trigger and note dispatch
    audioEngine.noteOn(channel = 0, key = 60, velocity = 100)
    audioEngine.pitchBend(channel = 0, bendNormalized = 0.5f)
    audioEngine.setModulationWheel(channel = 0, modNormalized = 0.8f)
    audioEngine.allNotesOff()

    audioEngine.release()
    assertEquals(com.example.audio.AudioEngine.State.RELEASED, audioEngine.engineState.value)
  }
}
