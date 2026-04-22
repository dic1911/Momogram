package moe.hx030.momogram.utils

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import java.util.Arrays
import moe.hx030.momogram.MomoConfig

object BufferUtil {
    @JvmStatic
    fun clear(buffer: Buffer?) {
        if (buffer == null) return

        buffer.clear()

        if (!buffer.hasArray() || buffer.isReadOnly || !MomoConfig.bufferCleaner.Bool()) return

        when (buffer) {
            is ByteBuffer -> Arrays.fill(buffer.array(), 0)
            is FloatBuffer -> Arrays.fill(buffer.array(), 0.0f)
            is IntBuffer -> Arrays.fill(buffer.array(), 0)
            is ShortBuffer -> Arrays.fill(buffer.array(), 0.toShort())
        }
    }
}
