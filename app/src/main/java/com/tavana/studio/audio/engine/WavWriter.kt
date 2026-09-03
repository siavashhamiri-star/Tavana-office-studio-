package com.tavana.studio.audio.engine

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Helper to write raw PCM 16-bit audio to standard RIFF/WAVE container.
 */
object WavWriter {

    fun writeWavHeader(
        raf: RandomAccessFile,
        sampleRate: Int,
        channels: Int,
        bitDepth: Int,
        totalPcmDataLen: Long
    ) {
        val totalDataLen = totalPcmDataLen + 36
        val byteRate = (sampleRate * channels * bitDepth / 8).toLong()
        val blockAlign = (channels * bitDepth / 8).toShort()

        raf.seek(0)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF chunk descriptor
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt(totalDataLen.toInt())
        header.put("WAVE".toByteArray(Charsets.US_ASCII))

        // "fmt " sub-chunk
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16) // SubChunk1Size for PCM
        header.putShort(1.toShort()) // AudioFormat 1 = PCM
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate.toInt())
        header.putShort(blockAlign)
        header.putShort(bitDepth.toShort())

        // "data" sub-chunk
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(totalPcmDataLen.toInt())

        raf.write(header.array())
    }

    fun finalizeWav(file: File, sampleRate: Int, channels: Int, bitDepth: Int) {
        if (!file.exists() || file.length() < 44) return
        val pcmLength = file.length() - 44
        RandomAccessFile(file, "rw").use { raf ->
            writeWavHeader(
                raf = raf,
                sampleRate = sampleRate,
                channels = channels,
                bitDepth = bitDepth,
                totalPcmDataLen = pcmLength
            )
        }
    }
}
