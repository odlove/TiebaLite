package com.huanchengfly.tieba.core.common.ext

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Context.getColorCompat(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)
