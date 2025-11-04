package com.huanchengfly.tieba.core.ui.theme.runtime

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.huanchengfly.tieba.core.common.ext.getColorCompat
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.CustomThemeConfig
import com.huanchengfly.tieba.core.ui.theme.ThemeCatalog
import com.huanchengfly.tieba.core.ui.theme.ThemePalette
import com.huanchengfly.tieba.core.ui.theme.ThemeSpec
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.core.ui.theme.ThemeType
import com.huanchengfly.tieba.core.ui.theme.TranslucentThemeConfig
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.dynamicTonalPalette
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt

interface ThemePaletteProvider {
    fun resolve(
        spec: ThemeSpec,
        useDynamicColor: Boolean,
        toolbarPrimary: Boolean,
        customConfig: CustomThemeConfig?,
        translucentConfig: TranslucentThemeConfig?
    ): ThemePalette

    companion object {
        /**
         * 为 Compose 场景创建临时实例（用于 UI 预览等）
         */
        fun createInstance(context: Context): ThemePaletteProvider {
            Log.i("ThemePaletteProvider", "createInstance context=$context")
            return ThemePaletteProviderImpl(context)
        }
    }
}

class ThemePaletteProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ThemePaletteProvider {
    private val logTag = "ThemePaletteProvider"

    override fun resolve(
        spec: ThemeSpec,
        useDynamicColor: Boolean,
        toolbarPrimary: Boolean,
        customConfig: CustomThemeConfig?,
        translucentConfig: TranslucentThemeConfig?
    ): ThemePalette {
        // 动态配色优先于其他类型
        if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && spec.supportsDynamicColor) {
            Log.i(
                logTag,
                "resolve dynamic themeKey=${spec.key} toolbarPrimary=$toolbarPrimary caller=${Throwable().stackTrace.getOrNull(2)}"
            )
            return resolveDynamicPalette(spec, toolbarPrimary)
        }

        return when (spec.type) {
            ThemeType.CUSTOM -> {
                val resolvedConfig = customConfig ?: defaultCustomConfig(toolbarPrimary)
                Log.i(
                    logTag,
                    "resolve custom themeKey=${spec.key} primary=${resolvedConfig.primaryColor} caller=${Throwable().stackTrace.getOrNull(2)}"
                )
                resolveCustomPalette(resolvedConfig)
            }
            ThemeType.TRANSLUCENT -> {
                val resolvedConfig = translucentConfig ?: defaultTranslucentConfig()
                Log.i(
                    logTag,
                    "resolve translucent themeKey=${spec.key} path=${resolvedConfig.backgroundPath} " +
                        "variant=${resolvedConfig.themeVariant} caller=${Throwable().stackTrace.getOrNull(2)}"
                )
                resolveTranslucentPalette(spec, resolvedConfig)
            }
            ThemeType.STATIC -> resolveStaticPalette(spec, toolbarPrimary)
        }
    }

    private fun defaultCustomConfig(toolbarPrimary: Boolean): CustomThemeConfig = CustomThemeConfig(
        primaryColor = context.getColorCompat(R.color.theme_color_primary_tieba),
        toolbarPrimary = toolbarPrimary,
        statusBarDark = !toolbarPrimary
    )

    private fun defaultTranslucentConfig(): TranslucentThemeConfig = TranslucentThemeConfig(
        backgroundPath = null,
        primaryColor = null,
        themeVariant = ThemeTokens.TRANSLUCENT_THEME_LIGHT,
        blur = 0,
        alpha = 255
    )

    private fun resolveDynamicPalette(spec: ThemeSpec, toolbarPrimary: Boolean): ThemePalette {
        val tonalPalette = dynamicTonalPalette(context)
        val themeKey = spec.key
        val isNight = spec.isNight

        val topBarColor = if (toolbarPrimary) tonalPalette.primary40 else if (isNight) tonalPalette.neutralVariant20 else tonalPalette.neutralVariant95
        val onTopBar = if (toolbarPrimary) tonalPalette.primary100 else if (isNight) tonalPalette.neutralVariant90 else tonalPalette.neutralVariant10
        val onTopBarSecondary = if (toolbarPrimary) tonalPalette.primary80 else if (isNight) tonalPalette.neutralVariant60 else tonalPalette.neutralVariant40
        val onTopBarActive = if (toolbarPrimary) tonalPalette.primary100 else if (isNight) tonalPalette.neutralVariant80 else tonalPalette.neutralVariant20
        val topBarSurface = if (toolbarPrimary) tonalPalette.primary90 else if (isNight) tonalPalette.neutralVariant20 else tonalPalette.neutralVariant95
        val onTopBarSurface = if (toolbarPrimary) tonalPalette.primary10 else if (isNight) tonalPalette.neutralVariant80 else tonalPalette.neutralVariant10

        val backgroundBase = when {
            themeKey == ThemeTokens.THEME_AMOLED_DARK -> tonalPalette.neutralVariant0
            isNight -> tonalPalette.neutralVariant10
            else -> tonalPalette.neutralVariant99
        }
        val cardBase = if (isNight) tonalPalette.neutralVariant20 else tonalPalette.neutralVariant99
        val floorCardBase = if (isNight) tonalPalette.neutralVariant20 else tonalPalette.neutralVariant95
        val chipBase = if (isNight) tonalPalette.neutralVariant20 else tonalPalette.neutralVariant95
        val navBar = if (isNight) tonalPalette.neutralVariant0 else tonalPalette.neutralVariant99
        val navBarSurface = if (isNight) tonalPalette.neutralVariant10 else tonalPalette.neutralVariant95
        val onNavBarSurface = if (isNight) tonalPalette.neutralVariant70 else tonalPalette.neutralVariant10

        return ThemePalette(
            primary = tonalPalette.primary40.toArgb(),
            primaryAlt = if (isNight) tonalPalette.primary80.toArgb() else tonalPalette.primary40.toArgb(),
            accent = tonalPalette.secondary40.toArgb(),
            onAccent = tonalPalette.secondary90.toArgb(),
            background = backgroundBase.toArgb(),
            windowBackground = backgroundBase.toArgb(),
            card = cardBase.toArgb(),
            floorCard = floorCardBase.toArgb(),
            chip = chipBase.toArgb(),
            onChip = (if (isNight) tonalPalette.neutralVariant80 else tonalPalette.neutralVariant30).toArgb(),
            textPrimary = tonalPalette.neutralVariant90.toArgb(),
            textSecondary = tonalPalette.neutralVariant70.toArgb(),
            textDisabled = tonalPalette.neutralVariant50.toArgb(),
            textOnPrimary = tonalPalette.neutralVariant0.toArgb(),
            toolbar = topBarColor.toArgb(),
            toolbarItem = onTopBar.toArgb(),
            toolbarItemActive = onTopBarActive.toArgb(),
            toolbarItemSecondary = onTopBarSecondary.toArgb(),
            toolbarSurface = topBarSurface.toArgb(),
            onToolbarSurface = onTopBarSurface.toArgb(),
            navBar = navBar.toArgb(),
            navBarSurface = navBarSurface.toArgb(),
            onNavBarSurface = onNavBarSurface.toArgb(),
            unselected = (if (isNight) tonalPalette.neutralVariant60 else tonalPalette.neutralVariant40).toArgb(),
            indicator = (if (isNight) tonalPalette.neutralVariant30 else tonalPalette.neutralVariant90).toArgb(),
            placeholder = (if (isNight) tonalPalette.neutralVariant60 else tonalPalette.neutralVariant40).toArgb(),
            divider = (if (isNight) tonalPalette.neutralVariant30 else tonalPalette.neutralVariant90).toArgb(),
            shadow = (if (isNight) tonalPalette.neutralVariant30 else tonalPalette.neutralVariant80).toArgb()
        )
    }

    private fun resolveCustomPalette(custom: CustomThemeConfig): ThemePalette {
        val primaryColor = custom.primaryColor
        val toolbarPrimary = custom.toolbarPrimary

        val toolbar = if (toolbarPrimary) primaryColor else context.getColorCompat(R.color.white)
        val toolbarItem = if (toolbarPrimary) context.getColorCompat(R.color.theme_color_toolbar_item_light) else context.getColorCompat(R.color.theme_color_toolbar_item_dark)
        val toolbarItemSecondary = if (toolbarPrimary) context.getColorCompat(R.color.theme_color_toolbar_item_secondary_white) else context.getColorCompat(R.color.theme_color_toolbar_item_secondary_light)
        val toolbarSurface = if (toolbarPrimary) primaryColor else context.getColorCompat(R.color.theme_color_toolbar_surface_light)
        val onToolbarSurface = if (toolbarPrimary) context.getColorCompat(R.color.theme_color_toolbar_item_secondary_white) else context.getColorCompat(R.color.theme_color_on_toolbar_surface_light)

        return ThemePalette(
            primary = primaryColor,
            primaryAlt = context.getColorCompat(R.color.theme_color_new_primary_light),
            accent = primaryColor,
            onAccent = context.getColorCompat(R.color.theme_color_on_accent_light),
            background = context.getColorCompat(R.color.theme_color_background_light),
            windowBackground = context.getColorCompat(R.color.theme_color_window_background_light),
            card = context.getColorCompat(R.color.theme_color_card_light),
            floorCard = context.getColorCompat(R.color.theme_color_floor_card_light),
            chip = context.getColorCompat(R.color.theme_color_chip_light),
            onChip = context.getColorCompat(R.color.theme_color_on_chip_light),
            textPrimary = context.getColorCompat(R.color.color_text),
            textSecondary = context.getColorCompat(R.color.color_text_secondary),
            textDisabled = context.getColorCompat(R.color.color_text_disabled),
            textOnPrimary = context.getColorCompat(R.color.theme_color_background_light),
            toolbar = toolbar,
            toolbarItem = toolbarItem,
            toolbarItemActive = primaryColor,
            toolbarItemSecondary = toolbarItemSecondary,
            toolbarSurface = toolbarSurface,
            onToolbarSurface = onToolbarSurface,
            navBar = context.getColorCompat(R.color.theme_color_nav_light),
            navBarSurface = context.getColorCompat(R.color.theme_color_nav_bar_surface_light),
            onNavBarSurface = context.getColorCompat(R.color.theme_color_on_nav_bar_surface_light),
            unselected = context.getColorCompat(R.color.theme_color_unselected_day),
            indicator = context.getColorCompat(R.color.default_color_swipe_refresh_view_background),
            placeholder = context.getColorCompat(R.color.theme_color_placeholder_light),
            divider = context.getColorCompat(R.color.theme_color_divider_light),
            shadow = context.getColorCompat(R.color.theme_color_shadow_day)
        )
    }

    private fun resolveTranslucentPalette(
        spec: ThemeSpec,
        config: TranslucentThemeConfig?
    ): ThemePalette {
        // 从 Catalog 获取结构化配置
        val catalogSpec = ThemePaletteCatalog.specs[spec.key]
        val basePalette = if (catalogSpec != null) {
            buildPaletteFromColorSet(catalogSpec.base)
        } else {
            val fallbackPrimary = config?.primaryColor ?: context.getColorCompat(R.color.theme_color_primary_tieba)
            buildPaletteFromColorRes(fallbackPrimary, spec.isNight)
        }

        return applyTranslucentOverrides(basePalette, config, spec.isNight)
    }

    private fun resolveStaticPalette(spec: ThemeSpec, toolbarPrimary: Boolean): ThemePalette {
        // 从 Catalog 获取结构化配置
        val catalogSpec = ThemePaletteCatalog.specs[spec.key]
        if (catalogSpec == null) {
            // 降级处理：如果主题未在 Catalog 中，使用默认浅色主题
            return buildPaletteFromColorRes(primary = context.getColorCompat(R.color.tieba), isNight = false)
        }

        // 获取基础调色板（lightPaletteBase）
        val base = catalogSpec.base
        var palette = buildPaletteFromColorSet(base, null)

        // 如果是夜间主题且有覆盖，应用 nightOverrides
        if (spec.isNight && catalogSpec.nightOverrides != null) {
            palette = applyNightOverrides(palette, catalogSpec.nightOverrides)
        }

        val preferPrimary = toolbarPrimary
        return applyToolbarPreference(
            palette = palette,
            usePrimary = preferPrimary,
            defaultUsesPrimary = catalogSpec.toolbarUsesPrimaryByDefault
        )
    }

    /**
     * 从 PaletteColorSet 构建 ThemePalette
     */
    private fun buildPaletteFromColorSet(colorSet: PaletteColorSet, overrides: PaletteOverrides? = null): ThemePalette {
        fun getColorOrOverride(@ColorRes original: Int, @ColorRes override: Int? = null): Int {
            return if (override != null) context.getColorCompat(override) else context.getColorCompat(original)
        }

        return ThemePalette(
            primary = getColorOrOverride(colorSet.primary, overrides?.primary),
            primaryAlt = getColorOrOverride(colorSet.primaryAlt, overrides?.primaryAlt),
            accent = getColorOrOverride(colorSet.accent, overrides?.accent),
            onAccent = getColorOrOverride(colorSet.onAccent, overrides?.onAccent),
            background = getColorOrOverride(colorSet.background, overrides?.background),
            windowBackground = getColorOrOverride(colorSet.windowBackground, overrides?.windowBackground),
            card = getColorOrOverride(colorSet.card, overrides?.card),
            floorCard = getColorOrOverride(colorSet.floorCard, overrides?.floorCard),
            chip = getColorOrOverride(colorSet.chip, overrides?.chip),
            onChip = getColorOrOverride(colorSet.onChip, overrides?.onChip),
            textPrimary = getColorOrOverride(colorSet.textPrimary, overrides?.textPrimary),
            textSecondary = getColorOrOverride(colorSet.textSecondary, overrides?.textSecondary),
            textDisabled = getColorOrOverride(colorSet.textDisabled, overrides?.textDisabled),
            textOnPrimary = getColorOrOverride(colorSet.textOnPrimary, overrides?.textOnPrimary),
            toolbar = getColorOrOverride(colorSet.toolbar, overrides?.toolbar),
            toolbarItem = getColorOrOverride(colorSet.toolbarItem, overrides?.toolbarItem),
            toolbarItemActive = getColorOrOverride(colorSet.toolbarItemActive, overrides?.toolbarItemActive),
            toolbarItemSecondary = getColorOrOverride(colorSet.toolbarItemSecondary, overrides?.toolbarItemSecondary),
            toolbarSurface = getColorOrOverride(colorSet.toolbarSurface, overrides?.toolbarSurface),
            onToolbarSurface = getColorOrOverride(colorSet.onToolbarSurface, overrides?.onToolbarSurface),
            navBar = getColorOrOverride(colorSet.navBar, overrides?.navBar),
            navBarSurface = getColorOrOverride(colorSet.navBarSurface, overrides?.navBarSurface),
            onNavBarSurface = getColorOrOverride(colorSet.onNavBarSurface, overrides?.onNavBarSurface),
            unselected = getColorOrOverride(colorSet.unselected, overrides?.unselected),
            indicator = getColorOrOverride(colorSet.indicator, overrides?.indicator),
            placeholder = getColorOrOverride(colorSet.placeholder, overrides?.placeholder),
            divider = getColorOrOverride(colorSet.divider, overrides?.divider),
            shadow = getColorOrOverride(colorSet.shadow, overrides?.shadow)
        )
    }

    /**
     * 应用夜间主题覆盖
     */
    private fun applyNightOverrides(palette: ThemePalette, overrides: PaletteOverrides): ThemePalette {
        return palette.copy(
            background = if (overrides.background != null) context.getColorCompat(overrides.background) else palette.background,
            windowBackground = if (overrides.windowBackground != null) context.getColorCompat(overrides.windowBackground) else palette.windowBackground,
            card = if (overrides.card != null) context.getColorCompat(overrides.card) else palette.card,
            floorCard = if (overrides.floorCard != null) context.getColorCompat(overrides.floorCard) else palette.floorCard,
            chip = if (overrides.chip != null) context.getColorCompat(overrides.chip) else palette.chip,
            onChip = if (overrides.onChip != null) context.getColorCompat(overrides.onChip) else palette.onChip,
            textPrimary = if (overrides.textPrimary != null) context.getColorCompat(overrides.textPrimary) else palette.textPrimary,
            textSecondary = if (overrides.textSecondary != null) context.getColorCompat(overrides.textSecondary) else palette.textSecondary,
            textDisabled = if (overrides.textDisabled != null) context.getColorCompat(overrides.textDisabled) else palette.textDisabled,
            onAccent = if (overrides.onAccent != null) context.getColorCompat(overrides.onAccent) else palette.onAccent,
            toolbar = if (overrides.toolbar != null) context.getColorCompat(overrides.toolbar) else palette.toolbar,
            toolbarItem = if (overrides.toolbarItem != null) context.getColorCompat(overrides.toolbarItem) else palette.toolbarItem,
            toolbarItemActive = if (overrides.toolbarItemActive != null) context.getColorCompat(overrides.toolbarItemActive) else palette.toolbarItemActive,
            toolbarItemSecondary = if (overrides.toolbarItemSecondary != null) context.getColorCompat(overrides.toolbarItemSecondary) else palette.toolbarItemSecondary,
            toolbarSurface = if (overrides.toolbarSurface != null) context.getColorCompat(overrides.toolbarSurface) else palette.toolbarSurface,
            onToolbarSurface = if (overrides.onToolbarSurface != null) context.getColorCompat(overrides.onToolbarSurface) else palette.onToolbarSurface,
            navBar = if (overrides.navBar != null) context.getColorCompat(overrides.navBar) else palette.navBar,
            navBarSurface = if (overrides.navBarSurface != null) context.getColorCompat(overrides.navBarSurface) else palette.navBarSurface,
            onNavBarSurface = if (overrides.onNavBarSurface != null) context.getColorCompat(overrides.onNavBarSurface) else palette.onNavBarSurface,
            unselected = if (overrides.unselected != null) context.getColorCompat(overrides.unselected) else palette.unselected,
            indicator = if (overrides.indicator != null) context.getColorCompat(overrides.indicator) else palette.indicator,
            placeholder = if (overrides.placeholder != null) context.getColorCompat(overrides.placeholder) else palette.placeholder,
            divider = if (overrides.divider != null) context.getColorCompat(overrides.divider) else palette.divider,
            shadow = if (overrides.shadow != null) context.getColorCompat(overrides.shadow) else palette.shadow
        )
    }

    /**
     * 降级方法：当 Catalog 未定义时使用
     */
    private fun buildPaletteFromColorRes(
        @ColorInt primary: Int,
        isNight: Boolean = false
    ): ThemePalette {
        val accentColor = context.getColorCompat(R.color.tieba)
        return ThemePalette(
            primary = primary,
            primaryAlt = if (isNight) context.getColorCompat(R.color.theme_color_new_primary_night) else context.getColorCompat(R.color.theme_color_new_primary_light),
            accent = accentColor,
            onAccent = context.getColorCompat(R.color.theme_color_on_accent_light),
            background = context.getColorCompat(if (isNight) R.color.theme_color_background_dark else R.color.theme_color_background_light),
            windowBackground = context.getColorCompat(if (isNight) R.color.theme_color_window_background_dark else R.color.theme_color_window_background_light),
            card = context.getColorCompat(if (isNight) R.color.theme_color_card_dark else R.color.theme_color_card_light),
            floorCard = context.getColorCompat(if (isNight) R.color.theme_color_floor_card_dark else R.color.theme_color_floor_card_light),
            chip = context.getColorCompat(if (isNight) R.color.theme_color_chip_dark else R.color.theme_color_chip_light),
            onChip = context.getColorCompat(if (isNight) R.color.theme_color_on_chip_dark else R.color.theme_color_on_chip_light),
            textPrimary = context.getColorCompat(if (isNight) R.color.color_text_night else R.color.color_text),
            textSecondary = context.getColorCompat(if (isNight) R.color.color_text_secondary_night else R.color.color_text_secondary),
            textDisabled = context.getColorCompat(if (isNight) R.color.color_text_disabled_night else R.color.color_text_disabled),
            textOnPrimary = context.getColorCompat(if (isNight) R.color.theme_color_background_dark else R.color.theme_color_background_light),
            toolbar = context.getColorCompat(if (isNight) R.color.theme_color_toolbar_dark else R.color.white),
            toolbarItem = context.getColorCompat(if (isNight) R.color.white else R.color.theme_color_toolbar_item_dark),
            toolbarItemActive = accentColor,
            toolbarItemSecondary = context.getColorCompat(if (isNight) R.color.theme_color_toolbar_item_secondary_dark else R.color.theme_color_toolbar_item_secondary_light),
            toolbarSurface = context.getColorCompat(if (isNight) R.color.theme_color_toolbar_surface_dark else R.color.theme_color_toolbar_surface_light),
            onToolbarSurface = context.getColorCompat(if (isNight) R.color.theme_color_on_toolbar_surface_dark else R.color.theme_color_on_toolbar_surface_light),
            navBar = context.getColorCompat(if (isNight) R.color.theme_color_nav_dark else R.color.theme_color_nav_light),
            navBarSurface = context.getColorCompat(if (isNight) R.color.theme_color_nav_bar_surface_dark else R.color.theme_color_nav_bar_surface_light),
            onNavBarSurface = context.getColorCompat(if (isNight) R.color.theme_color_on_nav_bar_surface_dark else R.color.theme_color_on_nav_bar_surface_light),
            unselected = context.getColorCompat(if (isNight) R.color.theme_color_unselected_dark else R.color.theme_color_unselected_day),
            indicator = context.getColorCompat(if (isNight) R.color.theme_color_indicator_dark else R.color.default_color_swipe_refresh_view_background),
            placeholder = context.getColorCompat(if (isNight) R.color.theme_color_placeholder_dark else R.color.theme_color_placeholder_light),
            divider = context.getColorCompat(if (isNight) R.color.theme_color_divider_dark else R.color.theme_color_divider_light),
            shadow = context.getColorCompat(if (isNight) R.color.theme_color_shadow_night else R.color.theme_color_shadow_day)
        )
    }


    private fun applyToolbarPreference(
        palette: ThemePalette,
        usePrimary: Boolean,
        defaultUsesPrimary: Boolean
    ): ThemePalette {
        if (!usePrimary && !defaultUsesPrimary) {
            return palette
        }

        if (usePrimary) {
            val primaryColor = palette.primary
            val isPrimaryDark = isColorDark(primaryColor)
            val toolbarItem = if (isPrimaryDark) {
                context.getColorCompat(R.color.theme_color_toolbar_item_dark)
            } else {
                context.getColorCompat(R.color.theme_color_toolbar_item_light)
            }
            val toolbarItemSecondary = if (isPrimaryDark) {
                context.getColorCompat(R.color.theme_color_toolbar_item_secondary_dark)
            } else {
                context.getColorCompat(R.color.theme_color_toolbar_item_secondary_white)
            }
            val onToolbarSurface = if (isPrimaryDark) {
                context.getColorCompat(R.color.theme_color_on_toolbar_surface_dark)
            } else {
                context.getColorCompat(R.color.theme_color_on_toolbar_surface_light)
            }
            return palette.copy(
                toolbar = primaryColor,
                toolbarItem = toolbarItem,
                toolbarItemSecondary = toolbarItemSecondary,
                toolbarItemActive = primaryColor,
                toolbarSurface = primaryColor,
                onToolbarSurface = onToolbarSurface
            )
        }

        if (!usePrimary && defaultUsesPrimary) {
            return palette.copy(
                toolbar = context.getColorCompat(R.color.white),
                toolbarItem = context.getColorCompat(R.color.theme_color_toolbar_item_dark),
                toolbarItemSecondary = context.getColorCompat(R.color.theme_color_toolbar_item_secondary_light),
                toolbarItemActive = palette.accent,
                toolbarSurface = context.getColorCompat(R.color.theme_color_toolbar_surface_light),
                onToolbarSurface = context.getColorCompat(R.color.theme_color_on_toolbar_surface_light)
            )
        }

        return palette
    }

    private fun applyTranslucentOverrides(
        palette: ThemePalette,
        config: TranslucentThemeConfig?,
        isNight: Boolean
    ): ThemePalette {
        val primaryColor = config?.primaryColor ?: return palette
        val primaryAlt = if (isNight) {
            darkenColor(primaryColor, 0.12f)
        } else {
            lightenColor(primaryColor, 0.12f)
        }
        return palette.copy(
            primary = primaryColor,
            primaryAlt = primaryAlt,
            accent = primaryColor,
            toolbarItemActive = primaryColor
        )
    }

    private fun lightenColor(@ColorInt color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = (hsv[1] - factor).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] + factor).coerceIn(0f, 1f)
        return Color.HSVToColor(Color.alpha(color), hsv)
    }

    private fun darkenColor(@ColorInt color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = (hsv[1] + factor).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] - factor).coerceIn(0f, 1f)
        return Color.HSVToColor(Color.alpha(color), hsv)
    }

    private fun androidx.compose.ui.graphics.Color.toArgb(): Int = (this.alpha * 255).roundToInt() shl 24 or
        (this.red * 255).roundToInt() shl 16 or
        (this.green * 255).roundToInt() shl 8 or
        (this.blue * 255).roundToInt()

    private fun isColorDark(@ColorInt color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
}
