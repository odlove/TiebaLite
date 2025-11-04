package com.huanchengfly.tieba.post.ui.common.theme

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.ThemePalette
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.post.getColorCompat
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class ThemeBridge @Inject constructor(
    private val themeController: ThemeController
) {

    val themeState: StateFlow<ThemeState>
        get() = themeController.themeState

    val currentState: ThemeState
        get() = themeController.themeState.value

    val palette: ThemePalette
        get() = currentState.palette

    fun colorByAttr(context: Context, @AttrRes attrId: Int): Int =
        ThemePaletteMapper.colorForAttr(palette, attrId) ?: resolveFallbackColor(context, attrId)

    fun colorById(context: Context, @ColorRes colorId: Int): Int =
        ThemePaletteMapper.colorForRes(palette, colorId) ?: context.getColorCompat(colorId)

    fun colorFromPalette(value: (ThemePalette) -> Int): Int = value(palette)

    fun resolveFallbackColor(context: Context, @AttrRes attrId: Int): Int {
        val fallbackRes = ThemeDefaults.resolveAttr(attrId)
        return context.getColorCompat(fallbackRes)
    }

    fun shouldUseDarkStatusBarIcons(): Boolean = themeController.shouldUseDarkStatusBarIcons()

    fun shouldUseDarkNavigationBarIcons(): Boolean = themeController.shouldUseDarkNavigationBarIcons()

}
