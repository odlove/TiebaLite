package com.huanchengfly.tieba.core.ui.theme.runtime

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.jvm.JvmStatic

object ThemeDrawableUtils {
    @JvmStatic
    fun tint(drawable: Drawable?, color: Int): Drawable? {
        if (drawable == null) return null
        val wrapper = DrawableCompat.wrap(drawable.mutate())
        DrawableCompat.setTint(wrapper, color)
        DrawableCompat.setTintMode(wrapper, android.graphics.PorterDuff.Mode.SRC_IN)
        return wrapper
    }

    @JvmStatic
    fun tint(drawable: Drawable?, colorStateList: ColorStateList): Drawable? {
        if (drawable == null) return null
        val wrapper = DrawableCompat.wrap(drawable.mutate())
        DrawableCompat.setTintList(wrapper, colorStateList)
        DrawableCompat.setTintMode(wrapper, android.graphics.PorterDuff.Mode.SRC_IN)
        return wrapper
    }
}
