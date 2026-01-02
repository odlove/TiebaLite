package com.huanchengfly.tieba.core.theme.runtime.palette

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.huanchengfly.tieba.core.common.ext.getColorCompat
import com.huanchengfly.tieba.core.theme.R
import com.huanchengfly.tieba.core.theme.model.CustomThemeConfig
import com.huanchengfly.tieba.core.theme.model.ThemeCatalog
import com.huanchengfly.tieba.core.theme.model.ThemePalette
import com.huanchengfly.tieba.core.theme.model.ThemeSpec
import com.huanchengfly.tieba.core.theme.model.ThemeTokens
import com.huanchengfly.tieba.core.theme.model.ThemeType
import com.huanchengfly.tieba.core.theme.model.TranslucentThemeConfig
import com.huanchengfly.tieba.core.theme.compose.dynamicTonalPalette
import androidx.core.graphics.ColorUtils
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
    ): ResolvedPalette

    companion object {
        /**
         * 为 Compose 场景创建临时实例（用于 UI 预览等）
         */
        fun createInstance(context: Context): ThemePaletteProvider {
            Log.i("ThemePaletteProvider", "createInstance context=$context")
            return ThemePaletteProviderImpl(context)
        }
    }

    data class ResolvedPalette(
        val palette: ThemePalette,
        val semanticColors: ThemeSemanticColors
    )
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
    ): ThemePaletteProvider.ResolvedPalette {
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
        primaryColor = context.getColorCompat(R.color.tieba),
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

    private fun resolveDynamicPalette(spec: ThemeSpec, toolbarPrimary: Boolean): ThemePaletteProvider.ResolvedPalette {
        val tonalPalette = dynamicTonalPalette(context)
        val variant = when {
            spec.key == ThemeTokens.THEME_AMOLED_DARK -> DynamicVariant.AMOLED
            spec.isNight -> DynamicVariant.DARK
            else -> DynamicVariant.LIGHT
        }

        val colors = DynamicColorSpec.resolveColors(tonalPalette, variant, toolbarPrimary)
        val palette = ThemePalette(
            primary = colors.primary,
            primaryAlt = colors.primaryAlt,
            accent = colors.accent,
            onAccent = colors.onAccent,
            background = colors.background,
            windowBackground = colors.windowBackground,
            card = colors.card,
            floorCard = colors.floorCard,
            chip = colors.chip,
            onChip = colors.onChip,
            textPrimary = colors.text,
            textSecondary = colors.textSecondary,
            textDisabled = colors.textDisabled,
            textOnPrimary = colors.onPrimary,
            toolbar = colors.topBar,
            toolbarItem = colors.onTopBar,
            toolbarItemActive = colors.onTopBarActive,
            toolbarItemSecondary = colors.onTopBarSecondary,
            toolbarSurface = colors.topBarSurface,
            onToolbarSurface = colors.onTopBarSurface,
            navBar = colors.bottomBar,
            navBarSurface = colors.bottomBarSurface,
            onNavBarSurface = colors.onBottomBarSurface,
            unselected = colors.unselected,
            indicator = colors.indicator,
            placeholder = colors.placeholder,
            divider = colors.divider,
            shadow = colors.shadow
        )
        val semantic = ThemeSemanticColors(
            surfacePrimary = colors.background,
            surfaceWindow = colors.windowBackground,
            surfaceCard = colors.card,
            surfaceFloor = colors.floorCard,
            surfaceChip = colors.chip,
            surfaceNav = colors.bottomBar,
            surfaceNavSurface = colors.bottomBarSurface,
            surfaceToolbar = colors.topBar,
            surfaceScrim = colors.shadow,
            contentPrimary = colors.text,
            contentSecondary = colors.textSecondary,
            contentDisabled = colors.textDisabled,
            contentInverse = colors.onPrimary,
            contentOnBrand = colors.onAccent,
            contentOnChip = colors.onChip,
            stateActive = colors.accent,
            stateUnselected = colors.unselected,
            stateIndicator = colors.indicator,
            outlineLow = colors.divider,
            outlineSurface = colors.onTopBarSurface,
            outlineSecondary = colors.onTopBarSecondary,
            decorPlaceholder = colors.placeholder,
            decorShadow = colors.shadow
        )
        return ThemePaletteProvider.ResolvedPalette(palette, semantic)
    }

    private fun resolveCustomPalette(custom: CustomThemeConfig): ThemePaletteProvider.ResolvedPalette {
        val primaryColor = custom.primaryColor
        val toolbarPrimary = custom.toolbarPrimary
        val primaryAlt = if (custom.statusBarDark) {
            darkenColor(primaryColor, 0.08f)
        } else {
            lightenColor(primaryColor, 0.08f)
        }
        val onAccent = if (isColorDark(primaryColor)) Color.WHITE else Color.BLACK
        val palette = ThemePalette(
            primary = primaryColor,
            primaryAlt = primaryAlt,
            accent = primaryColor,
            onAccent = onAccent,
            background = 0,
            windowBackground = 0,
            card = 0,
            floorCard = 0,
            chip = 0,
            onChip = 0,
            textPrimary = 0,
            textSecondary = 0,
            textDisabled = 0,
            textOnPrimary = onAccent,
            toolbar = 0,
            toolbarItem = 0,
            toolbarItemActive = primaryColor,
            toolbarItemSecondary = 0,
            toolbarSurface = 0,
            onToolbarSurface = 0,
            navBar = 0,
            navBarSurface = 0,
            onNavBarSurface = 0,
            unselected = 0,
            indicator = 0,
            placeholder = 0,
            divider = 0,
            shadow = 0
        )
        val semanticResult = applySemanticOverrides(palette, ThemeMode.LIGHT)
        val adjusted = applyToolbarPreference(
            semanticResult.palette,
            usePrimary = toolbarPrimary,
            defaultUsesPrimary = toolbarPrimary
        )
        val semantic = semanticResult.semanticColors.withToolbarFrom(adjusted)
        return ThemePaletteProvider.ResolvedPalette(adjusted, semantic)
    }

    private fun resolveTranslucentPalette(
        spec: ThemeSpec,
        config: TranslucentThemeConfig?
    ): ThemePaletteProvider.ResolvedPalette {
        // 从 Catalog 获取结构化配置
        val catalogSpec = ThemePaletteCatalog.specs[spec.key]
        var basePalette = if (catalogSpec != null) {
            buildPaletteFromColorSet(catalogSpec.base)
        } else {
            val fallbackPrimary = config?.primaryColor ?: context.getColorCompat(R.color.tieba)
            buildPaletteFromColorRes(fallbackPrimary, spec.isNight)
        }

        val semanticResult = applySemanticOverrides(basePalette, ThemeMode.TRANSLUCENT)
        val adjusted = applyTranslucentOverrides(semanticResult.palette, config, spec.isNight)
        val resolved = ThemePaletteProvider.ResolvedPalette(adjusted, semanticResult.semanticColors)
        return applyVariantOverrides(resolved, spec.key)
    }

    private fun resolveStaticPalette(spec: ThemeSpec, toolbarPrimary: Boolean): ThemePaletteProvider.ResolvedPalette {
        // 从 Catalog 获取结构化配置
        val catalogSpec = ThemePaletteCatalog.specs[spec.key]
        if (catalogSpec == null) {
            // 降级处理：如果主题未在 Catalog 中，使用默认浅色主题
            val fallback = buildPaletteFromColorRes(primary = context.getColorCompat(R.color.tieba), isNight = spec.isNight)
            val semanticResult = applySemanticOverrides(fallback, if (spec.isNight) ThemeMode.DARK else ThemeMode.LIGHT)
            val adjusted = applyToolbarPreference(
                palette = semanticResult.palette,
                usePrimary = toolbarPrimary,
                defaultUsesPrimary = false
            )
            val semantic = semanticResult.semanticColors.withToolbarFrom(adjusted)
            val resolved = ThemePaletteProvider.ResolvedPalette(adjusted, semantic)
            return applyVariantOverrides(resolved, spec.key)
        }

        // 获取基础调色板（lightPaletteBase）
        val base = catalogSpec.base
        val palette = buildPaletteFromColorSet(base)

        val semanticMode = if (spec.isNight) ThemeMode.DARK else ThemeMode.LIGHT
        val semanticResult = applySemanticOverrides(palette, semanticMode)

        val preferPrimary = toolbarPrimary
        val adjusted = applyToolbarPreference(
            palette = semanticResult.palette,
            usePrimary = preferPrimary,
            defaultUsesPrimary = catalogSpec.toolbarUsesPrimaryByDefault
        )
        val semantic = semanticResult.semanticColors.withToolbarFrom(adjusted)
        val resolved = ThemePaletteProvider.ResolvedPalette(adjusted, semantic)
        return applyVariantOverrides(resolved, spec.key)
    }

    /**
     * 从 PaletteColorSet 构建 ThemePalette
     */
    private fun buildPaletteFromColorSet(colorSet: PaletteColorSet): ThemePalette {
        val primary = context.getColorCompat(colorSet.primary)
        val primaryAlt = context.getColorCompat(colorSet.primaryAlt)
        val accent = context.getColorCompat(colorSet.accent)
        val onAccent = context.getColorCompat(colorSet.onAccent)
        return ThemePalette(
            primary = primary,
            primaryAlt = primaryAlt,
            accent = accent,
            onAccent = onAccent,
            background = 0,
            windowBackground = 0,
            card = 0,
            floorCard = 0,
            chip = 0,
            onChip = 0,
            textPrimary = 0,
            textSecondary = 0,
            textDisabled = 0,
            textOnPrimary = onAccent,
            toolbar = 0,
            toolbarItem = 0,
            toolbarItemActive = accent,
            toolbarItemSecondary = 0,
            toolbarSurface = 0,
            onToolbarSurface = 0,
            navBar = 0,
            navBarSurface = 0,
            onNavBarSurface = 0,
            unselected = 0,
            indicator = 0,
            placeholder = 0,
            divider = 0,
            shadow = 0
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
        val primaryAlt = if (isNight) {
            darkenColor(primary, 0.08f)
        } else {
            lightenColor(primary, 0.08f)
        }
        val onAccent = if (isColorDark(accentColor)) Color.WHITE else Color.BLACK
        return ThemePalette(
            primary = primary,
            primaryAlt = primaryAlt,
            accent = accentColor,
            onAccent = onAccent,
            background = 0,
            windowBackground = 0,
            card = 0,
            floorCard = 0,
            chip = 0,
            onChip = 0,
            textPrimary = 0,
            textSecondary = 0,
            textDisabled = 0,
            textOnPrimary = onAccent,
            toolbar = 0,
            toolbarItem = 0,
            toolbarItemActive = accentColor,
            toolbarItemSecondary = 0,
            toolbarSurface = 0,
            onToolbarSurface = 0,
            navBar = 0,
            navBarSurface = 0,
            onNavBarSurface = 0,
            unselected = 0,
            indicator = 0,
            placeholder = 0,
            divider = 0,
            shadow = 0
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
            val toolbarItem = if (isPrimaryDark) Color.WHITE else Color.BLACK
            val toolbarItemSecondary = ColorUtils.setAlphaComponent(toolbarItem, (0.65f * 255).roundToInt())
            val onToolbarSurface = if (isPrimaryDark) {
                lightenColor(primaryColor, 0.2f)
            } else {
                darkenColor(primaryColor, 0.2f)
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

        val neutralSurface = palette.toolbarSurface.takeIf { it != 0 } ?: palette.toolbar
        return palette.copy(
            toolbar = neutralSurface,
            toolbarItem = palette.textPrimary,
            toolbarItemSecondary = palette.textSecondary,
            toolbarItemActive = palette.accent,
            toolbarSurface = neutralSurface,
            onToolbarSurface = palette.divider
        )
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

    private fun applySemanticOverrides(palette: ThemePalette, mode: ThemeMode): ThemePaletteProvider.ResolvedPalette {
        val semantic = loadSemanticColors(mode)
        val updated = palette.copy(
            background = semantic.surfacePrimary,
            windowBackground = semantic.surfaceWindow,
            card = semantic.surfaceCard,
            floorCard = semantic.surfaceFloor,
            chip = semantic.surfaceChip,
            onChip = semantic.contentOnChip,
            textPrimary = semantic.contentPrimary,
            textSecondary = semantic.contentSecondary,
            textDisabled = semantic.contentDisabled,
            textOnPrimary = semantic.contentOnBrand,
            toolbar = semantic.surfaceToolbar,
            toolbarItem = semantic.contentPrimary,
            toolbarItemSecondary = semantic.contentSecondary,
            toolbarItemActive = palette.accent,
            toolbarSurface = semantic.surfaceToolbar,
            onToolbarSurface = semantic.outlineSurface,
            navBar = semantic.surfaceNav,
            navBarSurface = semantic.surfaceNavSurface,
            onNavBarSurface = semantic.outlineSurface,
            unselected = semantic.stateUnselected,
            indicator = semantic.stateIndicator,
            placeholder = semantic.decorPlaceholder,
            divider = semantic.outlineLow,
            shadow = semantic.decorShadow
        )
        return ThemePaletteProvider.ResolvedPalette(updated, semantic)
    }

    private fun applyVariantOverrides(
        resolved: ThemePaletteProvider.ResolvedPalette,
        themeKey: String
    ): ThemePaletteProvider.ResolvedPalette {
        val override = darkVariantSemanticOverrides[themeKey] ?: return resolved
        val overriddenSemantic = resolved.semanticColors.applyOverride(override)
        val palette = resolved.palette.copy(
            background = overriddenSemantic.surfacePrimary,
            windowBackground = overriddenSemantic.surfaceWindow,
            card = overriddenSemantic.surfaceCard,
            floorCard = overriddenSemantic.surfaceFloor,
            chip = overriddenSemantic.surfaceChip,
            onChip = resolveOverrideColor(override.contentOnChipRes, resolved.palette.onChip),
            navBar = overriddenSemantic.surfaceNav,
            navBarSurface = overriddenSemantic.surfaceNavSurface,
            toolbar = overriddenSemantic.surfaceToolbar,
            toolbarItem = resolveOverrideColor(override.toolbarItemRes, resolved.palette.toolbarItem),
            toolbarItemSecondary = resolveOverrideColor(
                override.toolbarItemSecondaryRes,
                resolved.palette.toolbarItemSecondary
            ),
            toolbarItemActive = resolveOverrideColor(
                override.toolbarItemActiveRes,
                resolved.palette.toolbarItemActive
            ),
            toolbarSurface = overriddenSemantic.surfaceToolbar,
            onToolbarSurface = overriddenSemantic.outlineSurface,
            unselected = resolveOverrideColor(override.stateUnselectedRes, resolved.palette.unselected),
            indicator = overriddenSemantic.stateIndicator,
            placeholder = overriddenSemantic.decorPlaceholder,
            divider = overriddenSemantic.outlineLow,
            shadow = overriddenSemantic.decorShadow
        )
        return ThemePaletteProvider.ResolvedPalette(palette, overriddenSemantic)
    }

    private fun ThemeSemanticColors.applyOverride(override: SemanticOverride): ThemeSemanticColors {
        return copy(
            surfacePrimary = resolveOverrideColor(override.surfacePrimaryRes, surfacePrimary),
            surfaceWindow = resolveOverrideColor(override.surfaceWindowRes, surfaceWindow),
            surfaceCard = resolveOverrideColor(override.surfaceCardRes, surfaceCard),
            surfaceFloor = resolveOverrideColor(override.surfaceFloorRes, surfaceFloor),
            surfaceChip = resolveOverrideColor(override.surfaceChipRes, surfaceChip),
            surfaceNav = resolveOverrideColor(override.surfaceNavRes, surfaceNav),
            surfaceNavSurface = resolveOverrideColor(override.surfaceNavSurfaceRes, surfaceNavSurface),
            surfaceToolbar = resolveOverrideColor(override.surfaceToolbarRes, surfaceToolbar),
            contentOnChip = resolveOverrideColor(override.contentOnChipRes, contentOnChip),
            outlineSurface = resolveOverrideColor(override.outlineSurfaceRes, outlineSurface),
            outlineLow = resolveOverrideColor(override.outlineLowRes, outlineLow),
            stateIndicator = resolveOverrideColor(override.stateIndicatorRes, stateIndicator),
            stateUnselected = resolveOverrideColor(override.stateUnselectedRes, stateUnselected),
            decorPlaceholder = resolveOverrideColor(override.decorPlaceholderRes, decorPlaceholder)
        )
    }

    private fun resolveOverrideColor(@ColorRes resId: Int?, fallback: Int): Int =
        resId?.let { context.getColorCompat(it) } ?: fallback

    private data class SemanticOverride(
        @ColorRes val surfacePrimaryRes: Int? = null,
        @ColorRes val surfaceWindowRes: Int? = null,
        @ColorRes val surfaceCardRes: Int? = null,
        @ColorRes val surfaceFloorRes: Int? = null,
        @ColorRes val surfaceChipRes: Int? = null,
        @ColorRes val surfaceNavRes: Int? = null,
        @ColorRes val surfaceNavSurfaceRes: Int? = null,
        @ColorRes val surfaceToolbarRes: Int? = null,
        @ColorRes val toolbarItemRes: Int? = null,
        @ColorRes val toolbarItemSecondaryRes: Int? = null,
        @ColorRes val toolbarItemActiveRes: Int? = null,
        @ColorRes val outlineSurfaceRes: Int? = null,
        @ColorRes val outlineLowRes: Int? = null,
        @ColorRes val stateIndicatorRes: Int? = null,
        @ColorRes val stateUnselectedRes: Int? = null,
        @ColorRes val decorPlaceholderRes: Int? = null,
        @ColorRes val contentOnChipRes: Int? = null
    )

    private val darkVariantSemanticOverrides = mapOf(
        ThemeTokens.THEME_BLUE_DARK to SemanticOverride(
            surfacePrimaryRes = R.color.theme_color_background_blue_dark,
            surfaceWindowRes = R.color.theme_color_window_background_blue_dark,
            surfaceCardRes = R.color.theme_color_card_blue_dark,
            surfaceFloorRes = R.color.theme_color_floor_card_blue_dark,
            surfaceChipRes = R.color.theme_color_chip_blue_dark,
            surfaceNavRes = R.color.theme_color_nav_blue_dark,
            surfaceNavSurfaceRes = R.color.theme_color_nav_bar_surface_blue_dark,
            surfaceToolbarRes = R.color.theme_color_toolbar_blue_dark,
            toolbarItemRes = R.color.theme_color_toolbar_item_dark,
            toolbarItemSecondaryRes = R.color.theme_color_toolbar_item_secondary_blue_dark,
            toolbarItemActiveRes = R.color.theme_color_toolbar_item_active_blue_dark,
            outlineSurfaceRes = R.color.theme_color_on_toolbar_surface_blue_dark,
            outlineLowRes = R.color.theme_color_divider_blue_dark,
            stateIndicatorRes = R.color.theme_color_indicator_blue_dark,
            stateUnselectedRes = R.color.theme_color_unselected_blue_dark,
            decorPlaceholderRes = R.color.theme_color_placeholder_blue_dark
        ),
        ThemeTokens.THEME_GREY_DARK to SemanticOverride(
            surfacePrimaryRes = R.color.theme_color_background_grey_dark,
            surfaceWindowRes = R.color.theme_color_window_background_grey_dark,
            surfaceCardRes = R.color.theme_color_card_grey_dark,
            surfaceFloorRes = R.color.theme_color_floor_card_grey_dark,
            surfaceChipRes = R.color.theme_color_chip_grey_dark,
            surfaceNavRes = R.color.theme_color_nav_grey_dark,
            surfaceNavSurfaceRes = R.color.theme_color_nav_bar_surface_grey_dark,
            surfaceToolbarRes = R.color.theme_color_toolbar_grey_dark,
            toolbarItemRes = R.color.theme_color_toolbar_item_dark,
            toolbarItemSecondaryRes = R.color.theme_color_toolbar_item_secondary_grey_dark,
            toolbarItemActiveRes = R.color.theme_color_toolbar_item_active_grey_dark,
            outlineSurfaceRes = R.color.theme_color_on_toolbar_surface_grey_dark,
            outlineLowRes = R.color.theme_color_divider_grey_dark,
            stateIndicatorRes = R.color.theme_color_indicator_grey_dark,
            stateUnselectedRes = R.color.theme_color_unselected_grey_dark,
            decorPlaceholderRes = R.color.theme_color_placeholder_grey_dark,
            contentOnChipRes = R.color.theme_color_on_chip_grey_dark
        ),
        ThemeTokens.THEME_AMOLED_DARK to SemanticOverride(
            surfacePrimaryRes = R.color.theme_color_background_amoled_dark,
            surfaceWindowRes = R.color.theme_color_window_background_amoled_dark,
            surfaceCardRes = R.color.theme_color_card_amoled_dark,
            surfaceFloorRes = R.color.theme_color_floor_card_amoled_dark,
            surfaceChipRes = R.color.theme_color_chip_amoled_dark,
            surfaceNavRes = R.color.theme_color_nav_amoled_dark,
            surfaceNavSurfaceRes = R.color.theme_color_nav_bar_surface_amoled_dark,
            surfaceToolbarRes = R.color.theme_color_toolbar_amoled_dark,
            toolbarItemRes = R.color.theme_color_toolbar_item_dark,
            toolbarItemSecondaryRes = R.color.theme_color_toolbar_item_secondary_amoled_dark,
            toolbarItemActiveRes = R.color.theme_color_toolbar_item_active_amoled_dark,
            outlineSurfaceRes = R.color.theme_color_on_toolbar_surface_amoled_dark,
            outlineLowRes = R.color.theme_color_divider_amoled_dark,
            stateIndicatorRes = R.color.theme_color_indicator_amoled_dark,
            stateUnselectedRes = R.color.theme_color_unselected_amoled_dark,
            decorPlaceholderRes = R.color.theme_color_placeholder_amoled_dark,
            contentOnChipRes = R.color.theme_color_on_chip_amoled_dark
        ),
        ThemeTokens.THEME_TRANSLUCENT_DARK to SemanticOverride(
            surfacePrimaryRes = R.color.theme_color_background_translucent_dark,
            surfaceWindowRes = R.color.theme_color_window_background_translucent_dark,
            surfaceCardRes = R.color.theme_color_card_translucent_dark,
            surfaceFloorRes = R.color.theme_color_floor_card_translucent_dark,
            surfaceChipRes = R.color.theme_color_chip_translucent_dark,
            surfaceNavRes = R.color.theme_color_nav_translucent_dark,
            surfaceNavSurfaceRes = R.color.theme_color_nav_bar_surface_translucent_dark,
            surfaceToolbarRes = R.color.theme_color_toolbar_translucent_dark,
            toolbarItemRes = R.color.theme_color_toolbar_item_translucent_dark,
            toolbarItemSecondaryRes = R.color.theme_color_toolbar_item_secondary_translucent_dark,
            toolbarItemActiveRes = R.color.theme_color_toolbar_item_active_translucent_dark,
            outlineSurfaceRes = R.color.theme_color_on_toolbar_surface_translucent_dark,
            outlineLowRes = R.color.theme_color_divider_translucent_dark,
            stateIndicatorRes = R.color.theme_color_indicator_translucent_dark,
            stateUnselectedRes = R.color.theme_color_unselected_translucent_dark,
            decorPlaceholderRes = R.color.theme_color_placeholder_translucent_dark
        )
    )

    private fun loadSemanticColors(mode: ThemeMode): ThemeSemanticColors {
        fun color(@ColorRes lightRes: Int, @ColorRes darkRes: Int, @ColorRes translucentRes: Int): Int {
            val res = when (mode) {
                ThemeMode.LIGHT -> lightRes
                ThemeMode.DARK -> darkRes
                ThemeMode.TRANSLUCENT -> translucentRes
            }
            return context.getColorCompat(res)
        }

        return ThemeSemanticColors(
            surfacePrimary = color(
                R.color.color_sem_surface_primary_light,
                R.color.color_sem_surface_primary_dark,
                R.color.color_sem_surface_primary_translucent
            ),
            surfaceWindow = color(
                R.color.color_sem_surface_window_light,
                R.color.color_sem_surface_window_dark,
                R.color.color_sem_surface_window_translucent
            ),
            surfaceCard = color(
                R.color.color_sem_surface_card_light,
                R.color.color_sem_surface_card_dark,
                R.color.color_sem_surface_card_translucent
            ),
            surfaceFloor = color(
                R.color.color_sem_surface_floor_light,
                R.color.color_sem_surface_floor_dark,
                R.color.color_sem_surface_floor_translucent
            ),
            surfaceChip = color(
                R.color.color_sem_surface_chip_light,
                R.color.color_sem_surface_chip_dark,
                R.color.color_sem_surface_chip_translucent
            ),
            surfaceNav = color(
                R.color.color_sem_surface_nav_light,
                R.color.color_sem_surface_nav_dark,
                R.color.color_sem_surface_nav_translucent
            ),
            surfaceNavSurface = color(
                R.color.color_sem_surface_nav_surface_light,
                R.color.color_sem_surface_nav_surface_dark,
                R.color.color_sem_surface_nav_surface_translucent
            ),
            surfaceToolbar = color(
                R.color.color_sem_surface_toolbar_light,
                R.color.color_sem_surface_toolbar_dark,
                R.color.color_sem_surface_toolbar_translucent
            ),
            surfaceScrim = color(
                R.color.color_sem_surface_scrim_light,
                R.color.color_sem_surface_scrim_dark,
                R.color.color_sem_surface_scrim_translucent
            ),
            contentPrimary = color(
                R.color.color_sem_content_primary_light,
                R.color.color_sem_content_primary_dark,
                R.color.color_sem_content_primary_translucent
            ),
            contentSecondary = color(
                R.color.color_sem_content_secondary_light,
                R.color.color_sem_content_secondary_dark,
                R.color.color_sem_content_secondary_translucent
            ),
            contentDisabled = color(
                R.color.color_sem_content_disabled_light,
                R.color.color_sem_content_disabled_dark,
                R.color.color_sem_content_disabled_translucent
            ),
            contentInverse = color(
                R.color.color_sem_content_inverse_light,
                R.color.color_sem_content_inverse_dark,
                R.color.color_sem_content_inverse_translucent
            ),
            contentOnBrand = color(
                R.color.color_sem_content_on_brand_light,
                R.color.color_sem_content_on_brand_dark,
                R.color.color_sem_content_on_brand_translucent
            ),
            contentOnChip = color(
                R.color.color_sem_content_on_chip_light,
                R.color.color_sem_content_on_chip_dark,
                R.color.color_sem_content_on_chip_translucent
            ),
            stateActive = color(
                R.color.color_sem_state_active_light,
                R.color.color_sem_state_active_dark,
                R.color.color_sem_state_active_translucent
            ),
            stateUnselected = color(
                R.color.color_sem_state_unselected_light,
                R.color.color_sem_state_unselected_dark,
                R.color.color_sem_state_unselected_translucent
            ),
            stateIndicator = color(
                R.color.color_sem_state_indicator_light,
                R.color.color_sem_state_indicator_dark,
                R.color.color_sem_state_indicator_translucent
            ),
            outlineLow = color(
                R.color.color_sem_outline_low_light,
                R.color.color_sem_outline_low_dark,
                R.color.color_sem_outline_low_translucent
            ),
            outlineSurface = color(
                R.color.color_sem_outline_surface_light,
                R.color.color_sem_outline_surface_dark,
                R.color.color_sem_outline_surface_translucent
            ),
            outlineSecondary = color(
                R.color.color_sem_outline_secondary_light,
                R.color.color_sem_outline_secondary_dark,
                R.color.color_sem_outline_secondary_translucent
            ),
            decorPlaceholder = color(
                R.color.color_sem_decor_placeholder_light,
                R.color.color_sem_decor_placeholder_dark,
                R.color.color_sem_decor_placeholder_translucent
            ),
            decorShadow = color(
                R.color.color_sem_decor_shadow_light,
                R.color.color_sem_decor_shadow_dark,
                R.color.color_sem_decor_shadow_translucent
            )
        )
    }

    private data class SemanticColors(
        val surfacePrimary: Int,
        val surfaceWindow: Int,
        val surfaceCard: Int,
        val surfaceFloor: Int,
        val surfaceChip: Int,
        val surfaceNav: Int,
        val surfaceNavSurface: Int,
        val surfaceToolbar: Int,
        val surfaceScrim: Int,
        val contentPrimary: Int,
        val contentSecondary: Int,
        val contentDisabled: Int,
        val contentInverse: Int,
        val contentOnBrand: Int,
        val contentOnChip: Int,
        val stateActive: Int,
        val stateUnselected: Int,
        val stateIndicator: Int,
        val outlineLow: Int,
        val outlineSurface: Int,
        val outlineSecondary: Int,
        val decorPlaceholder: Int,
        val decorShadow: Int
    )

    private enum class ThemeMode {
        LIGHT,
        DARK,
        TRANSLUCENT
    }

    private fun ThemeSemanticColors.withToolbarFrom(palette: ThemePalette): ThemeSemanticColors =
        copy(
            surfaceToolbar = palette.toolbar,
            outlineSurface = palette.onToolbarSurface
        )

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
