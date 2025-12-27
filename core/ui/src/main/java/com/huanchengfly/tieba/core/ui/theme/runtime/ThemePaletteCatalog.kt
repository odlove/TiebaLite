package com.huanchengfly.tieba.core.ui.theme.runtime

import androidx.annotation.ColorRes
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.core.ui.theme.ThemeType

internal data class PaletteColorSet(
    @ColorRes val primary: Int,
    @ColorRes val primaryAlt: Int,
    @ColorRes val accent: Int,
    @ColorRes val onAccent: Int
)

internal data class PaletteSpec(
    val key: String,
    val type: ThemeType,
    val base: PaletteColorSet,
    val toolbarUsesPrimaryByDefault: Boolean = false,
    val supportsDynamicColor: Boolean = true
)

internal object ThemePaletteCatalog {
    private fun resolveLightPrimaryRes(themeKeySuffix: String): Int = when (themeKeySuffix) {
        "blue" -> R.color.theme_color_primary_blue
        "pink" -> R.color.theme_color_primary_pink
        "red" -> R.color.theme_color_primary_red
        "purple" -> R.color.theme_color_primary_purple
        "black" -> R.color.theme_color_primary_black
        else -> R.color.theme_color_primary_tieba
    }

    private fun resolveLightAccentRes(themeKeySuffix: String): Int = when (themeKeySuffix) {
        "blue" -> R.color.theme_color_accent_blue
        "pink" -> R.color.theme_color_accent_pink
        "red" -> R.color.theme_color_accent_red
        "purple" -> R.color.theme_color_accent_purple
        "black" -> R.color.theme_color_accent_black
        else -> R.color.theme_color_accent_tieba
    }

    private fun lightPaletteBase(themeKeySuffix: String = ""): PaletteColorSet {
        val primaryRes = resolveLightPrimaryRes(themeKeySuffix)
        val accentRes = resolveLightAccentRes(themeKeySuffix)
        return PaletteColorSet(
            primary = primaryRes,
            primaryAlt = primaryRes,
            accent = accentRes,
            onAccent = R.color.theme_color_on_accent_light
        )
    }

    private fun darkPaletteBase(
        primaryRes: Int,
        accentRes: Int,
        onAccentRes: Int
    ): PaletteColorSet = PaletteColorSet(
        primary = primaryRes,
        primaryAlt = R.color.theme_color_new_primary_night,
        accent = accentRes,
        onAccent = onAccentRes
    )

    private val staticSpecs = listOf(
        // 浅色主题
        PaletteSpec(
            key = ThemeTokens.THEME_DEFAULT,
            type = ThemeType.STATIC,
            base = lightPaletteBase(),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_BLUE,
            type = ThemeType.STATIC,
            base = lightPaletteBase("blue"),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_PINK,
            type = ThemeType.STATIC,
            base = lightPaletteBase("pink"),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_RED,
            type = ThemeType.STATIC,
            base = lightPaletteBase("red"),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_PURPLE,
            type = ThemeType.STATIC,
            base = lightPaletteBase("purple"),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_BLACK,
            type = ThemeType.STATIC,
            base = lightPaletteBase("black"),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),

        // 深色主题
        PaletteSpec(
            key = ThemeTokens.THEME_BLUE_DARK,
            type = ThemeType.STATIC,
            base = darkPaletteBase(
                primaryRes = R.color.theme_color_primary_blue_dark,
                accentRes = R.color.theme_color_accent_blue_dark,
                onAccentRes = R.color.theme_color_on_accent_blue_dark
            ),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = false
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_GREY_DARK,
            type = ThemeType.STATIC,
            base = darkPaletteBase(
                primaryRes = R.color.theme_color_primary_grey_dark,
                accentRes = R.color.theme_color_accent_grey_dark,
                onAccentRes = R.color.theme_color_on_accent_grey_dark
            ),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = false
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_AMOLED_DARK,
            type = ThemeType.STATIC,
            base = darkPaletteBase(
                primaryRes = R.color.theme_color_primary_amoled_dark,
                accentRes = R.color.theme_color_accent_amoled_dark,
                onAccentRes = R.color.theme_color_on_accent_amoled_dark
            ),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = false
        ),

        // 透明主题
        PaletteSpec(
            key = ThemeTokens.THEME_TRANSLUCENT,
            type = ThemeType.TRANSLUCENT,
            base = PaletteColorSet(
                primary = R.color.theme_color_primary_tieba,
                primaryAlt = R.color.theme_color_new_primary_light,
                accent = R.color.theme_color_accent_tieba,
                onAccent = R.color.theme_color_on_accent_translucent_light
            ),
            supportsDynamicColor = false
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_TRANSLUCENT_LIGHT,
            type = ThemeType.TRANSLUCENT,
            base = PaletteColorSet(
                primary = R.color.theme_color_primary_tieba,
                primaryAlt = R.color.theme_color_new_primary_light,
                accent = R.color.theme_color_accent_tieba,
                onAccent = R.color.theme_color_on_accent_translucent_light
            ),
            supportsDynamicColor = false
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_TRANSLUCENT_DARK,
            type = ThemeType.TRANSLUCENT,
            base = PaletteColorSet(
                primary = R.color.theme_color_primary_dark,
                primaryAlt = R.color.theme_color_new_primary_night,
                accent = R.color.theme_color_accent_dark,
                onAccent = R.color.theme_color_on_accent_translucent_dark
            ),
            supportsDynamicColor = false
        )
    )

    val specs: Map<String, PaletteSpec> = staticSpecs.associateBy { it.key }
}
