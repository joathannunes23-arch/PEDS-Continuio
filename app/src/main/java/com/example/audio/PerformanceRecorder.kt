package com.example.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * High-quality 16-bit stereo WAV performance recorder.
 * Max limit: 5 minutes (300 seconds).
 */
class PerformanceRecorder(private val context: Context) {

    companion object {
        const val MAX_RECORDING_SECONDS = 300 // 5 minutes max
        const val SAMPLE_RATE = WorshipSynthEngine.SAMPLE_RATE
        const val CHANNELS = 2
        const val BYTES_PER_SAMPLE = 2
    }

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private val _lastSavedFilePath = MutableStateFlow<String?>(null)
    val lastSavedFilePath = _lastSavedFilePath.asStateFlow()

    private var currentFile: File? = null
    private var outputStream: FileOutputStream? = null
    private var totalBytesWritten: Long = 0
    private var samplesWrittenCount: Long = 0

    // Preallocated byte buffer to eliminate allocations on audio thread
    private val pcmByteBuffer = ByteBuffer.allocate(1024 * CHANNELS * BYTES_PER_SAMPLE).order(ByteOrder.LITTLE_ENDIAN)
    private val pcmByteArray = ByteArray(1024 * CHANNELS * BYTES_PER_SAMPLE)

    fun startRecording(): Boolean {
        if (_isRecording.value) return false

        try {
            val recDir = File(context.cacheDir, "recordings").apply { mkdirs() }
            val fileName = "WorshipPad_${System.currentTimeMillis()}.wav"
            val file = File(recDir, fileName)
            currentFile = file
            outputStream = FileOutputStream(file)
            totalBytesWritten = 0
            samplesWrittenCount = 0
            _elapsedSeconds.value = 0

            // Write blank 44-byte WAV header (will update lengths at end)
            val dummyHeader = ByteArray(44)
            outputStream?.write(dummyHeader)

            _isRecording.value = true
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun onAudioSamples(buffer: FloatArray, frameCount: Int) {
        if (!_isRecording.value) return
        val stream = outputStream ?: return

        try {
            val bytesNeeded = frameCount * CHANNELS * BYTES_PER_SAMPLE
            val targetByteArray = if (bytesNeeded <= pcmByteArray.size) pcmByteArray else ByteArray(bytesNeeded)
            val byteBuffer = if (bytesNeeded <= pcmByteBuffer.capacity()) {
                pcmByteBuffer.clear()
                pcmByteBuffer
            } else {
                ByteBuffer.allocate(bytesNeeded).order(ByteOrder.LITTLE_ENDIAN)
            }

            for (i in 0 until frameCount) {
                val s = (buffer[i] * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                byteBuffer.putShort(s) // Left
                byteBuffer.putShort(s) // Right
            }

            byteBuffer.flip()
            byteBuffer.get(targetByteArray, 0, bytesNeeded)
            stream.write(targetByteArray, 0, bytesNeeded)
            totalBytesWritten += bytesNeeded
            samplesWrittenCount += frameCount

            val seconds = (samplesWrittenCount / SAMPLE_RATE).toInt()
            if (_elapsedSeconds.value != seconds) {
                _elapsedSeconds.value = seconds
            }

            if (seconds >= MAX_RECORDING_SECONDS) {
                stopRecording()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording(): String? {
        if (!_isRecording.value) return null
        _isRecording.value = false

        try {
            outputStream?.flush()
            outputStream?.close()
            outputStream = null

            val file = currentFile ?: return null

            // Overwrite correct WAV header sizes
            RandomAccessFile(file, "rw").use { raf ->
                raf.seek(0)
                writeWavHeader(raf, totalBytesWritten, SAMPLE_RATE, CHANNELS)
            }

            val path = file.absolutePath
            _lastSavedFilePath.value = path
            return path
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun shareLastRecording(context: Context) {
        val path = _lastSavedFilePath.value ?: return
        val file = File(path)
        if (!file.exists()) return

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar Gravação Worship"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun writeWavHeader(raf: RandomAccessFile, pcmDataLength: Long, sampleRate: Int, channels: Int) {
        val totalDataLen = pcmDataLength + 36
        val byteRate = sampleRate * channels * 2

        raf.writeBytes("RIFF")
        raf.write(intToByteArray(totalDataLen.toInt()))
        raf.writeBytes("WAVE")
        raf.writeBytes("fmt ")
        raf.write(intToByteArray(16)) // Subchunk1Size for PCM
        raf.write(shortToByteArray(1)) // AudioFormat 1 = PCM
        raf.write(shortToByteArray(channels.toShort()))
        raf.write(intToByteArray(sampleRate))
        raf.write(intToByteArray(byteRate))
        raf.write(shortToByteArray((channels * 2).toShort())) // BlockAlign
        raf.write(shortToByteArray(16)) // BitsPerSample
        raf.writeBytes("data")
        raf.write(intToByteArray(pcmDataLength.toInt()))
    }

    private fun intToByteArray(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
    }
}
