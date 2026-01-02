package com.huanchengfly.tieba.core.theme.runtime.palette

import androidx.compose.ui.graphics.toArgb
import com.huanchengfly.tieba.core.theme.compose.TonalPalette

internal enum class DynamicVariant { LIGHT, DARK, AMOLED }

internal data class DynamicColorValues(
    val primary: Int,
    val primaryAlt: Int,
    val accent: Int,
    val onAccent: Int,
    val background: Int,
    val windowBackground: Int,
    val card: Int,
    val floorCard: Int,
    val chip: Int,
    val onChip: Int,
    val text: Int,
    val textSecondary: Int,
    val textDisabled: Int,
    val onPrimary: Int,
    val topBar: Int,
    val onTopBar: Int,
    val onTopBarActive: Int,
    val onTopBarSecondary: Int,
    val topBarSurface: Int,
    val onTopBarSurface: Int,
    val bottomBar: Int,
    val bottomBarSurface: Int,
    val onBottomBarSurface: Int,
    val unselected: Int,
    val indicator: Int,
    val placeholder: Int,
    val divider: Int,
    val shadow: Int
)

internal object DynamicColorSpec {
    fun resolveColors(
        palette: TonalPalette,
        variant: DynamicVariant,
        toolbarPrimary: Boolean
    ): DynamicColorValues {
        val primary = when (variant) {
            DynamicVariant.LIGHT -> palette.primary40
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.primary80
        }.toArgb()

        val primaryAlt = when (variant) {
            DynamicVariant.LIGHT -> palette.primary40
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.primary80
        }.toArgb()

        val accent = when (variant) {
            DynamicVariant.LIGHT -> palette.secondary40
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.secondary80
        }.toArgb()
        val onAccent = when (variant) {
            DynamicVariant.LIGHT -> palette.secondary100
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.secondary20
        }.toArgb()

        val backgroundColor = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant99
            DynamicVariant.DARK -> palette.neutralVariant10
            DynamicVariant.AMOLED -> palette.neutralVariant0
        }.toArgb()

        val card = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant99
            DynamicVariant.DARK -> palette.neutralVariant20
            DynamicVariant.AMOLED -> palette.neutralVariant10
        }.toArgb()
        val floorCard = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant95
            DynamicVariant.DARK -> palette.neutralVariant20
            DynamicVariant.AMOLED -> palette.neutralVariant10
        }.toArgb()
        val chip = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant95
            DynamicVariant.DARK -> palette.neutralVariant20
            DynamicVariant.AMOLED -> palette.neutralVariant10
        }.toArgb()
        val onChip = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant40
            DynamicVariant.DARK -> palette.neutralVariant60
            DynamicVariant.AMOLED -> palette.neutralVariant50
        }.toArgb()

        val text = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant10
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant90
        }.toArgb()
        val textSecondary = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant40
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant70
        }.toArgb()
        val textDisabled = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant70
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant50
        }.toArgb()

        val onPrimary = when (variant) {
            DynamicVariant.LIGHT -> palette.primary100
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.primary10
        }.toArgb()

        val (topBar, onTopBar, onTopBarSecondary, onTopBarActive, topBarSurface, onTopBarSurface) =
            resolveTopBarColors(palette, variant, toolbarPrimary)

        val bottomBar = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant99
            DynamicVariant.DARK -> palette.neutralVariant10
            DynamicVariant.AMOLED -> palette.neutralVariant0
        }.toArgb()
        val bottomBarSurface = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant95
            DynamicVariant.DARK -> palette.neutralVariant20
            DynamicVariant.AMOLED -> palette.neutralVariant10
        }.toArgb()
        val onBottomBarSurface = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant30
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant70
        }.toArgb()

        val unselected = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant60
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant40
        }.toArgb()

        val indicator = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant95
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant10
        }.toArgb()

        val placeholder = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant100
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant50
        }.toArgb()

        val divider = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant95
            DynamicVariant.DARK -> palette.neutralVariant20
            DynamicVariant.AMOLED -> palette.neutralVariant10
        }.toArgb()
        val shadow = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant90
            DynamicVariant.DARK -> palette.neutralVariant20
            DynamicVariant.AMOLED -> palette.neutralVariant10
        }.toArgb()

        return DynamicColorValues(
            primary = primary,
            primaryAlt = primaryAlt,
            accent = accent,
            onAccent = onAccent,
            background = backgroundColor,
            windowBackground = backgroundColor,
            card = card,
            floorCard = floorCard,
            chip = chip,
            onChip = onChip,
            text = text,
            textSecondary = textSecondary,
            textDisabled = textDisabled,
            onPrimary = onPrimary,
            topBar = topBar,
            onTopBar = onTopBar,
            onTopBarActive = onTopBarActive,
            onTopBarSecondary = onTopBarSecondary,
            topBarSurface = topBarSurface,
            onTopBarSurface = onTopBarSurface,
            bottomBar = bottomBar,
            bottomBarSurface = bottomBarSurface,
            onBottomBarSurface = onBottomBarSurface,
            unselected = unselected,
            indicator = indicator,
            placeholder = placeholder,
            divider = divider,
            shadow = shadow
        )
    }

    private data class TopBarColors(
        val topBar: Int,
        val onTopBar: Int,
        val onTopBarSecondary: Int,
        val onTopBarActive: Int,
        val topBarSurface: Int,
        val onTopBarSurface: Int
    )

    private fun resolveTopBarColors(
        palette: TonalPalette,
        variant: DynamicVariant,
        toolbarPrimary: Boolean
    ): TopBarColors {
        if (toolbarPrimary) {
            return TopBarColors(
                topBar = palette.primary40.toArgb(),
                onTopBar = palette.primary100.toArgb(),
                onTopBarSecondary = palette.primary80.toArgb(),
                onTopBarActive = palette.primary100.toArgb(),
                topBarSurface = palette.primary90.toArgb(),
                onTopBarSurface = palette.primary10.toArgb()
            )
        }
        val topBar = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant99
            DynamicVariant.DARK -> palette.neutralVariant10
            DynamicVariant.AMOLED -> palette.neutralVariant0
        }.toArgb()
        val onTopBar = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant10
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant90
        }.toArgb()
        val onTopBarSecondary = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant40
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant70
        }.toArgb()
        val onTopBarActive = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant20
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant80
        }.toArgb()
        val topBarSurface = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant99
            DynamicVariant.DARK -> palette.neutralVariant20
            DynamicVariant.AMOLED -> palette.neutralVariant10
        }.toArgb()
        val onTopBarSurface = when (variant) {
            DynamicVariant.LIGHT -> palette.neutralVariant30
            DynamicVariant.DARK,
            DynamicVariant.AMOLED -> palette.neutralVariant70
        }.toArgb()
        return TopBarColors(
            topBar = topBar,
            onTopBar = onTopBar,
            onTopBarSecondary = onTopBarSecondary,
            onTopBarActive = onTopBarActive,
            topBarSurface = topBarSurface,
            onTopBarSurface = onTopBarSurface
        )
    }
}
