package com.huanchengfly.tieba.core.common.util

import java.io.ByteArrayOutputStream

object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567="

    fun encode(bytes: ByteArray): String {
        val outputStream = ByteArrayOutputStream()
        for (i in 0 until (bytes.size + 4) / 5) {
            val shorts = ShortArray(5)
            val ints = IntArray(8)
            var filled = 5
            for (j in 0 until 5) {
                val index = i * 5 + j
                if (index < bytes.size) {
                    shorts[j] = (bytes[index].toInt() and 0xFF).toShort()
                } else {
                    shorts[j] = 0
                    filled--
                }
            }
            val padding = transformPadding(filled)
            ints[0] = (shorts[0].toInt() shr 3 and 31)
            ints[1] = (shorts[0].toInt() and 7 shl 2 or (shorts[1].toInt() shr 6 and 3))
            ints[2] = (shorts[1].toInt() shr 1 and 31)
            ints[3] = (shorts[1].toInt() and 1 shl 4 or (shorts[2].toInt() shr 4 and 15))
            ints[4] = (shorts[2].toInt() and 15 shl 1 or (shorts[3].toInt() shr 7 and 1))
            ints[5] = (shorts[3].toInt() shr 2 and 31)
            ints[6] = (shorts[3].toInt() and 3 shl 3 or (shorts[4].toInt() shr 5 and 7))
            ints[7] = (shorts[4].toInt() and 31)
            for (k in 0 until ints.size - padding) {
                outputStream.write(ALPHABET[ints[k]].code)
            }
        }
        return outputStream.toString()
    }

    private fun transformPadding(filled: Int): Int = when (filled) {
        1 -> 6
        2 -> 4
        3 -> 3
        4 -> 1
        5 -> 0
        else -> -1
    }
}
