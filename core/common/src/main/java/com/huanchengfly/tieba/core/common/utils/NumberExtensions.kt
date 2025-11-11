package com.huanchengfly.tieba.core.common.utils

fun String.getShortNumString(): String {
    val long = toLongOrNull() ?: return ""
    return long.getShortNumString()
}

fun Int.getShortNumString(): String {
    return toLong().getShortNumString()
}

fun Long.getShortNumString(): String {
    return if (this > 9999) {
        val longW = this * 10 / 10000L / 10F
        if (longW > 999) {
            val longKW = longW.toLong() * 10 / 1000L / 10F
            "${longKW}KW"
        } else {
            "${longW}W"
        }
    } else {
        "$this"
    }
}
