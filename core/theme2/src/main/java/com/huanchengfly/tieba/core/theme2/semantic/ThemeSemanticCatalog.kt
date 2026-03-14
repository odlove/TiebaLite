package com.huanchengfly.tieba.core.theme2.semantic

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeContent
import com.huanchengfly.tieba.core.theme2.model.ThemeChannel
import com.huanchengfly.tieba.core.theme2.model.ThemeSemanticColors

data class ThemeSeedLight(
    @ColorInt val brand: Int
)

data class ThemeSeedDark(
    @ColorInt val brand: Int,
    @ColorInt val neutralDark: Int
)

object ThemeSemanticCatalog {
    const val THEME_TIEBA = "tieba"
    const val THEME_DARK_BLACK = "amoled_dark"

    private const val DARK_NAV_BLEND = 0.09f

    private val defaultLightSeed = ThemeSeedLight(
        brand = 0xFF4477E0.toInt()
    )

    private val darkBlackSeed = ThemeSeedDark(
        brand = 0xFF4477E0.toInt(),
        neutralDark = 0xFF000000.toInt()
    )

    fun light(seed: ThemeSeedLight = defaultLightSeed): ThemeSemanticColors =
        run {
            val scheme = SchemeContent(
                Hct.fromInt(seed.brand),
                false,
                0.0
            )
            ThemeSemanticColors(
                surfacePrimary = scheme.surface,
                surfaceNav = scheme.surface,
                stateActive = scheme.primary,
                stateUnselected = ColorUtils.blendARGB(scheme.primary, scheme.surface, 0.6f),
                contentOnBrand = scheme.onSecondary,
                outlineLow = scheme.outline
            )
        }

    fun dark(seed: ThemeSeedDark = darkBlackSeed): ThemeSemanticColors =
        ThemeSemanticColors(
            surfacePrimary = seed.neutralDark,
            surfaceNav = ColorUtils.blendARGB(seed.neutralDark, 0xFFFFFFFF.toInt(), DARK_NAV_BLEND),
            stateActive = seed.brand,
            stateUnselected = 0xFF415C68.toInt(),
            contentOnBrand = 0xFF131D28.toInt(),
            outlineLow = 0xFF10171D.toInt()
        )

    fun resolve(themeKey: String?, channel: ThemeChannel): ThemeSemanticColors =
        when (themeKey) {
            THEME_DARK_BLACK -> dark(darkBlackSeed)
            THEME_TIEBA -> light()
            else -> if (channel == ThemeChannel.NIGHT) dark(darkBlackSeed) else light()
        }
}
