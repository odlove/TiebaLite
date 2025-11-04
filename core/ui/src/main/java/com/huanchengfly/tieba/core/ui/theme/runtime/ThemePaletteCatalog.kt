package com.huanchengfly.tieba.core.ui.theme.runtime

import androidx.annotation.ColorRes
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.core.ui.theme.ThemeType

internal data class PaletteColorSet(
    @ColorRes val primary: Int,
    @ColorRes val primaryAlt: Int,
    @ColorRes val accent: Int,
    @ColorRes val background: Int,
    @ColorRes val windowBackground: Int,
    @ColorRes val card: Int,
    @ColorRes val floorCard: Int,
    @ColorRes val chip: Int,
    @ColorRes val onChip: Int,
    @ColorRes val toolbar: Int,
    @ColorRes val toolbarItem: Int,
    @ColorRes val toolbarItemActive: Int,
    @ColorRes val toolbarItemSecondary: Int,
    @ColorRes val toolbarSurface: Int,
    @ColorRes val onToolbarSurface: Int,
    @ColorRes val navBar: Int,
    @ColorRes val navBarSurface: Int,
    @ColorRes val onNavBarSurface: Int,
    @ColorRes val unselected: Int,
    @ColorRes val textPrimary: Int,
    @ColorRes val textSecondary: Int,
    @ColorRes val textDisabled: Int,
    @ColorRes val textOnPrimary: Int,
    @ColorRes val onAccent: Int,
    @ColorRes val divider: Int,
    @ColorRes val placeholder: Int,
    @ColorRes val indicator: Int,
    @ColorRes val shadow: Int
)

internal data class PaletteOverrides(
    @ColorRes val primary: Int? = null,
    @ColorRes val primaryAlt: Int? = null,
    @ColorRes val accent: Int? = null,
    @ColorRes val background: Int? = null,
    @ColorRes val windowBackground: Int? = null,
    @ColorRes val card: Int? = null,
    @ColorRes val floorCard: Int? = null,
    @ColorRes val chip: Int? = null,
    @ColorRes val onChip: Int? = null,
    @ColorRes val toolbar: Int? = null,
    @ColorRes val toolbarItem: Int? = null,
    @ColorRes val toolbarItemActive: Int? = null,
    @ColorRes val toolbarItemSecondary: Int? = null,
    @ColorRes val toolbarSurface: Int? = null,
    @ColorRes val onToolbarSurface: Int? = null,
    @ColorRes val navBar: Int? = null,
    @ColorRes val navBarSurface: Int? = null,
    @ColorRes val onNavBarSurface: Int? = null,
    @ColorRes val unselected: Int? = null,
    @ColorRes val textPrimary: Int? = null,
    @ColorRes val textSecondary: Int? = null,
    @ColorRes val textDisabled: Int? = null,
    @ColorRes val textOnPrimary: Int? = null,
    @ColorRes val onAccent: Int? = null,
    @ColorRes val divider: Int? = null,
    @ColorRes val placeholder: Int? = null,
    @ColorRes val indicator: Int? = null,
    @ColorRes val shadow: Int? = null
)

internal data class PaletteSpec(
    val key: String,
    val type: ThemeType,
    val base: PaletteColorSet,
    val nightOverrides: PaletteOverrides? = null,
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

    // 白色背景通用配置
    private fun lightPaletteBase(themeKeySuffix: String = ""): PaletteColorSet {
        val primaryRes = resolveLightPrimaryRes(themeKeySuffix)
        val accentRes = resolveLightAccentRes(themeKeySuffix)
        return PaletteColorSet(
            primary = primaryRes,
            primaryAlt = primaryRes,
            accent = accentRes,
            background = R.color.theme_color_background_light,
            windowBackground = R.color.theme_color_window_background_light,
            card = R.color.theme_color_card_light,
            floorCard = R.color.theme_color_floor_card_light,
            chip = R.color.theme_color_chip_light,
            onChip = R.color.theme_color_on_chip_light,
            toolbar = R.color.white,
            toolbarItem = R.color.theme_color_toolbar_item_dark,
            toolbarItemActive = accentRes,
            toolbarItemSecondary = R.color.theme_color_toolbar_item_secondary_light,
            toolbarSurface = R.color.theme_color_toolbar_surface_light,
            onToolbarSurface = R.color.theme_color_on_toolbar_surface_light,
            navBar = R.color.theme_color_nav_light,
            navBarSurface = R.color.theme_color_nav_bar_surface_light,
            onNavBarSurface = R.color.theme_color_on_nav_bar_surface_light,
            unselected = R.color.theme_color_unselected_day,
            textPrimary = R.color.color_text,
            textSecondary = R.color.color_text_secondary,
            textDisabled = R.color.color_text_disabled,
            textOnPrimary = R.color.theme_color_background_light,
            onAccent = R.color.theme_color_on_accent_light,
            divider = R.color.theme_color_divider_light,
            placeholder = R.color.theme_color_placeholder_light,
            indicator = R.color.default_color_swipe_refresh_view_background,
            shadow = R.color.theme_color_shadow_day
        )
    }

    // 夜间主题通用覆盖
    private fun darkNightOverrides() = PaletteOverrides(
        background = R.color.theme_color_background_dark,
        windowBackground = R.color.theme_color_window_background_dark,
        card = R.color.theme_color_card_dark,
        floorCard = R.color.theme_color_floor_card_dark,
        chip = R.color.theme_color_chip_dark,
        onChip = R.color.theme_color_on_chip_dark,
        toolbar = R.color.theme_color_toolbar_dark,
        toolbarItem = R.color.theme_color_toolbar_item_dark,
        toolbarItemSecondary = R.color.theme_color_toolbar_item_secondary_dark,
        toolbarItemActive = R.color.theme_color_toolbar_item_active_dark,
        toolbarSurface = R.color.theme_color_toolbar_surface_dark,
        onToolbarSurface = R.color.theme_color_on_toolbar_surface_dark,
        navBar = R.color.theme_color_nav_dark,
        navBarSurface = R.color.theme_color_nav_bar_surface_dark,
        onNavBarSurface = R.color.theme_color_on_nav_bar_surface_dark,
        unselected = R.color.theme_color_unselected_dark,
        textPrimary = R.color.color_text_night,
        textSecondary = R.color.color_text_secondary_night,
        textDisabled = R.color.color_text_disabled_night,
        onAccent = R.color.theme_color_on_accent_dark,
        divider = R.color.theme_color_divider_dark,
        placeholder = R.color.theme_color_placeholder_dark,
        indicator = R.color.theme_color_indicator_dark,
        shadow = R.color.theme_color_shadow_night
    )

    private val staticSpecs = listOf(
        // 浅色主题
        PaletteSpec(
            key = ThemeTokens.THEME_DEFAULT,
            type = ThemeType.STATIC,
            base = lightPaletteBase(),
            nightOverrides = darkNightOverrides(),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_BLUE,
            type = ThemeType.STATIC,
            base = lightPaletteBase("blue"),
            nightOverrides = darkNightOverrides(),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_PINK,
            type = ThemeType.STATIC,
            base = lightPaletteBase("pink"),
            nightOverrides = darkNightOverrides(),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_RED,
            type = ThemeType.STATIC,
            base = lightPaletteBase("red"),
            nightOverrides = darkNightOverrides(),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_PURPLE,
            type = ThemeType.STATIC,
            base = lightPaletteBase("purple"),
            nightOverrides = darkNightOverrides(),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_BLACK,
            type = ThemeType.STATIC,
            base = lightPaletteBase("black"),
            nightOverrides = darkNightOverrides(),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = true
        ),

        // 深色主题
        PaletteSpec(
            key = ThemeTokens.THEME_BLUE_DARK,
            type = ThemeType.STATIC,
            base = PaletteColorSet(
                primary = R.color.theme_color_primary_blue_dark,
                primaryAlt = R.color.theme_color_new_primary_night,
                accent = R.color.theme_color_accent_blue_dark,
                background = R.color.theme_color_background_blue_dark,
                windowBackground = R.color.theme_color_window_background_blue_dark,
                card = R.color.theme_color_card_blue_dark,
                floorCard = R.color.theme_color_floor_card_blue_dark,
                chip = R.color.theme_color_chip_blue_dark,
                onChip = R.color.theme_color_on_chip_blue_dark,
                toolbar = R.color.theme_color_toolbar_blue_dark,
                toolbarItem = R.color.theme_color_toolbar_item_dark,
                toolbarItemActive = R.color.theme_color_toolbar_item_active_blue_dark,
                toolbarItemSecondary = R.color.theme_color_toolbar_item_secondary_blue_dark,
                toolbarSurface = R.color.theme_color_toolbar_surface_blue_dark,
                onToolbarSurface = R.color.theme_color_on_toolbar_surface_blue_dark,
                navBar = R.color.theme_color_nav_blue_dark,
                navBarSurface = R.color.theme_color_nav_bar_surface_blue_dark,
                onNavBarSurface = R.color.theme_color_on_nav_bar_surface_dark,
                unselected = R.color.theme_color_unselected_blue_dark,
                textPrimary = R.color.color_text_night,
                textSecondary = R.color.color_text_secondary_night,
                textDisabled = R.color.color_text_disabled_night,
                textOnPrimary = R.color.color_text_night,
                onAccent = R.color.theme_color_on_accent_blue_dark,
                divider = R.color.theme_color_divider_blue_dark,
                placeholder = R.color.theme_color_placeholder_blue_dark,
                indicator = R.color.theme_color_indicator_blue_dark,
                shadow = R.color.theme_color_shadow_night
            ),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = false
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_GREY_DARK,
            type = ThemeType.STATIC,
            base = PaletteColorSet(
                primary = R.color.theme_color_primary_grey_dark,
                primaryAlt = R.color.theme_color_new_primary_night,
                accent = R.color.theme_color_accent_grey_dark,
                background = R.color.theme_color_background_grey_dark,
                windowBackground = R.color.theme_color_window_background_grey_dark,
                card = R.color.theme_color_card_grey_dark,
                floorCard = R.color.theme_color_floor_card_grey_dark,
                chip = R.color.theme_color_chip_grey_dark,
                onChip = R.color.theme_color_on_chip_grey_dark,
                toolbar = R.color.theme_color_toolbar_grey_dark,
                toolbarItem = R.color.theme_color_toolbar_item_dark,
                toolbarItemActive = R.color.theme_color_toolbar_item_active_grey_dark,
                toolbarItemSecondary = R.color.theme_color_toolbar_item_secondary_grey_dark,
                toolbarSurface = R.color.theme_color_toolbar_surface_grey_dark,
                onToolbarSurface = R.color.theme_color_on_toolbar_surface_grey_dark,
                navBar = R.color.theme_color_nav_grey_dark,
                navBarSurface = R.color.theme_color_nav_bar_surface_grey_dark,
                onNavBarSurface = R.color.theme_color_on_nav_bar_surface_dark,
                unselected = R.color.theme_color_unselected_grey_dark,
                textPrimary = R.color.color_text_night,
                textSecondary = R.color.color_text_secondary_night,
                textDisabled = R.color.color_text_disabled_night,
                textOnPrimary = R.color.color_text_night,
                onAccent = R.color.theme_color_on_accent_grey_dark,
                divider = R.color.theme_color_divider_grey_dark,
                placeholder = R.color.theme_color_placeholder_grey_dark,
                indicator = R.color.theme_color_indicator_grey_dark,
                shadow = R.color.theme_color_shadow_night
            ),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = false
        ),
        PaletteSpec(
            key = ThemeTokens.THEME_AMOLED_DARK,
            type = ThemeType.STATIC,
            base = PaletteColorSet(
                primary = R.color.theme_color_primary_amoled_dark,
                primaryAlt = R.color.theme_color_new_primary_night,
                accent = R.color.theme_color_accent_amoled_dark,
                background = R.color.theme_color_background_amoled_dark,
                windowBackground = R.color.theme_color_window_background_amoled_dark,
                card = R.color.theme_color_card_amoled_dark,
                floorCard = R.color.theme_color_floor_card_amoled_dark,
                chip = R.color.theme_color_chip_amoled_dark,
                onChip = R.color.theme_color_on_chip_amoled_dark,
                toolbar = R.color.theme_color_toolbar_amoled_dark,
                toolbarItem = R.color.theme_color_toolbar_item_dark,
                toolbarItemActive = R.color.theme_color_toolbar_item_active_amoled_dark,
                toolbarItemSecondary = R.color.theme_color_toolbar_item_secondary_amoled_dark,
                toolbarSurface = R.color.theme_color_toolbar_surface_amoled_dark,
                onToolbarSurface = R.color.theme_color_on_toolbar_surface_amoled_dark,
                navBar = R.color.theme_color_nav_amoled_dark,
                navBarSurface = R.color.theme_color_nav_bar_surface_amoled_dark,
                onNavBarSurface = R.color.theme_color_on_nav_bar_surface_dark,
                unselected = R.color.theme_color_unselected_amoled_dark,
                textPrimary = R.color.color_text_night,
                textSecondary = R.color.color_text_secondary_night,
                textDisabled = R.color.color_text_disabled_night,
                textOnPrimary = R.color.color_text_night,
                onAccent = R.color.theme_color_on_accent_amoled_dark,
                divider = R.color.theme_color_divider_amoled_dark,
                placeholder = R.color.theme_color_placeholder_amoled_dark,
                indicator = R.color.theme_color_indicator_amoled_dark,
                shadow = R.color.theme_color_shadow_night
            ),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = false
        ),

        // 透明主题变体
        PaletteSpec(
            key = "translucent_light",
            type = ThemeType.TRANSLUCENT,
            base = PaletteColorSet(
                primary = R.color.theme_color_primary_tieba,
                primaryAlt = R.color.theme_color_new_primary_light,
                accent = R.color.theme_color_accent_tieba,
                background = R.color.theme_color_background_light,
                windowBackground = R.color.theme_color_window_background_light,
                card = R.color.theme_color_card_translucent_light,
                floorCard = R.color.theme_color_floor_card_translucent_light,
                chip = R.color.theme_color_chip_translucent_light,
                onChip = R.color.theme_color_on_chip_translucent_light,
                toolbar = R.color.theme_color_toolbar_translucent_light,
                toolbarItem = R.color.theme_color_toolbar_item_translucent_light,
                toolbarItemActive = R.color.theme_color_toolbar_item_active_translucent_light,
                toolbarItemSecondary = R.color.theme_color_toolbar_item_secondary_translucent_light,
                toolbarSurface = R.color.theme_color_toolbar_surface_translucent_light,
                onToolbarSurface = R.color.theme_color_on_toolbar_surface_translucent_light,
                navBar = R.color.theme_color_nav_light,
                navBarSurface = R.color.theme_color_nav_bar_surface_light,
                onNavBarSurface = R.color.theme_color_on_nav_bar_surface_light,
                unselected = R.color.theme_color_unselected_translucent_light,
                textPrimary = R.color.color_text_translucent_light,
                textSecondary = R.color.color_text_secondary_translucent_light,
                textDisabled = R.color.color_text_disabled_translucent_light,
                textOnPrimary = R.color.theme_color_background_light,
                onAccent = R.color.theme_color_on_accent_translucent_light,
                divider = R.color.theme_color_divider_translucent_light,
                placeholder = R.color.theme_color_placeholder_translucent_light,
                indicator = R.color.theme_color_indicator_translucent_light,
                shadow = R.color.theme_color_shadow_day
            ),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = false
        ),
        PaletteSpec(
            key = "translucent_dark",
            type = ThemeType.TRANSLUCENT,
            base = PaletteColorSet(
                primary = R.color.theme_color_primary_dark,
                primaryAlt = R.color.theme_color_new_primary_night,
                accent = R.color.theme_color_accent_dark,
                background = R.color.theme_color_background_dark,
                windowBackground = R.color.theme_color_window_background_dark,
                card = R.color.theme_color_card_translucent_dark,
                floorCard = R.color.theme_color_floor_card_translucent_dark,
                chip = R.color.theme_color_chip_translucent_dark,
                onChip = R.color.theme_color_on_chip_translucent_dark,
                toolbar = R.color.theme_color_toolbar_translucent_dark,
                toolbarItem = R.color.theme_color_toolbar_item_translucent_dark,
                toolbarItemActive = R.color.theme_color_toolbar_item_active_translucent_dark,
                toolbarItemSecondary = R.color.theme_color_toolbar_item_secondary_translucent_dark,
                toolbarSurface = R.color.theme_color_toolbar_surface_translucent_dark,
                onToolbarSurface = R.color.theme_color_on_toolbar_surface_translucent_dark,
                navBar = R.color.theme_color_nav_dark,
                navBarSurface = R.color.theme_color_nav_bar_surface_dark,
                onNavBarSurface = R.color.theme_color_on_nav_bar_surface_dark,
                unselected = R.color.theme_color_unselected_translucent_dark,
                textPrimary = R.color.color_text_translucent_dark,
                textSecondary = R.color.color_text_secondary_translucent_dark,
                textDisabled = R.color.color_text_disabled_translucent_dark,
                textOnPrimary = R.color.color_text_night,
                onAccent = R.color.theme_color_on_accent_translucent_dark,
                divider = R.color.theme_color_divider_translucent_dark,
                placeholder = R.color.theme_color_placeholder_translucent_dark,
                indicator = R.color.theme_color_indicator_translucent_dark,
                shadow = R.color.theme_color_shadow_night
            ),
            toolbarUsesPrimaryByDefault = false,
            supportsDynamicColor = false
        )
    )

    val specs: Map<String, PaletteSpec> = staticSpecs.associateBy { it.key }
}
