package com.huanchengfly.tieba.core.ui.theme

import androidx.annotation.ColorInt

data class ThemePalette(
    @ColorInt val primary: Int,
    @ColorInt val primaryAlt: Int,
    @ColorInt val accent: Int,
    @ColorInt val onAccent: Int,
    @ColorInt val background: Int,
    @ColorInt val windowBackground: Int,
    @ColorInt val card: Int,
    @ColorInt val floorCard: Int,
    @ColorInt val chip: Int,
    @ColorInt val onChip: Int,
    @ColorInt val textPrimary: Int,
    @ColorInt val textSecondary: Int,
    @ColorInt val textDisabled: Int,
    @ColorInt val textOnPrimary: Int,
    @ColorInt val toolbar: Int,
    @ColorInt val toolbarItem: Int,
    @ColorInt val toolbarItemActive: Int,
    @ColorInt val toolbarItemSecondary: Int,
    @ColorInt val toolbarSurface: Int,
    @ColorInt val onToolbarSurface: Int,
    @ColorInt val navBar: Int,
    @ColorInt val navBarSurface: Int,
    @ColorInt val onNavBarSurface: Int,
    @ColorInt val unselected: Int,
    @ColorInt val indicator: Int,
    @ColorInt val placeholder: Int,
    @ColorInt val divider: Int,
    @ColorInt val shadow: Int
)

data class CustomThemeConfig(
    @ColorInt val primaryColor: Int,
    val toolbarPrimary: Boolean,
    val statusBarDark: Boolean
)

data class TranslucentThemeConfig(
    val backgroundPath: String?,
    @ColorInt val primaryColor: Int?,
    val themeVariant: Int,
    val blur: Int,
    val alpha: Int
)
