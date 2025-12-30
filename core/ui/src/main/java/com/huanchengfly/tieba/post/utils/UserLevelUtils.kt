package com.huanchengfly.tieba.post.utils

import androidx.annotation.ColorInt

@ColorInt
fun getIconColorByLevel(levelStr: String?): Int {
    var color = 0xFFB7BCB6.toInt()
    if (levelStr == null) return color
    color = when (levelStr) {
        "1", "2", "3" -> 0xFF2FBEAB.toInt()
        "4", "5", "6", "7", "8", "9" -> 0xFF3AA7E9.toInt()
        "10", "11", "12", "13", "14", "15" -> 0xFFFFA126.toInt()
        "16", "17", "18" -> 0xFFFF9C19.toInt()
        else -> color
    }
    return ColorUtils.greifyColor(color, 0.2f)
}
