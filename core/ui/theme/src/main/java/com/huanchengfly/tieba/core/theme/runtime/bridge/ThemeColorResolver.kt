package com.huanchengfly.tieba.core.theme.runtime.bridge

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.huanchengfly.tieba.core.theme.R
import com.huanchengfly.tieba.core.theme.model.ThemeState
import com.huanchengfly.tieba.core.theme.compose.ExtendedColors
import com.huanchengfly.tieba.core.theme.compose.resolveTopBarContentColor
import com.huanchengfly.tieba.core.theme.compose.resolveTopBarSecondaryColor
import com.huanchengfly.tieba.core.theme.compose.toExtendedColors
import com.huanchengfly.tieba.core.theme.runtime.entrypoints.ThemeRuntimeEntryPoint
import dagger.hilt.android.EntryPointAccessors

object ThemeColorResolver {
    private fun themeBridge(context: Context): ThemeBridge {
        val applicationContext = context.applicationContext
        return EntryPointAccessors.fromApplication(
            applicationContext,
            ThemeRuntimeEntryPoint::class.java
        ).themeBridge()
    }

    @JvmStatic
    fun colorByAttr(context: Context, @AttrRes attrId: Int): Int =
        themeBridge(context).colorByAttr(context, attrId)

    @JvmStatic
    fun colorById(context: Context, @ColorRes colorId: Int): Int =
        themeBridge(context).colorById(context, colorId)

    @JvmStatic
    fun state(context: Context): ThemeState =
        themeBridge(context).currentState

    @JvmStatic
    fun extendedColors(context: Context): ExtendedColors =
        state(context).toExtendedColors()

    private fun colorOf(context: Context, valueProvider: (ExtendedColors) -> Color): Int =
        valueProvider(extendedColors(context)).toArgb()

    @JvmStatic
    fun primaryColor(context: Context): Int = colorOf(context) { it.primary }

    @JvmStatic
    fun onPrimaryColor(context: Context): Int = colorOf(context) { it.onPrimary }

    @JvmStatic
    fun accentColor(context: Context): Int = colorOf(context) { it.accent }

    @JvmStatic
    fun onAccentColor(context: Context): Int = colorOf(context) { it.onAccent }

    @JvmStatic
    fun topBarColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceToolbar

    @JvmStatic
    fun topBarContentColor(context: Context): Int =
        themeBridge(context).palette.toolbarItem

    @JvmStatic
    fun topBarSubtitleColor(context: Context): Int =
        themeBridge(context).palette.toolbarItemSecondary

    @JvmStatic
    fun topBarSurfaceColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceToolbar

    @JvmStatic
    fun onTopBarSurfaceColor(context: Context): Int =
        themeBridge(context).semanticColors.outlineSurface

    @JvmStatic
    fun navBarColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceNav

    @JvmStatic
    fun navBarSurfaceColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceNavSurface

    @JvmStatic
    fun navBarContentColor(context: Context): Int =
        colorOf(context) { it.onBottomBarSurface }

    @JvmStatic
    fun textColor(context: Context): Int =
        themeBridge(context).semanticColors.contentPrimary

    @JvmStatic
    fun textSecondaryColor(context: Context): Int =
        themeBridge(context).semanticColors.contentSecondary

    @JvmStatic
    fun textDisabledColor(context: Context): Int =
        themeBridge(context).semanticColors.contentDisabled

    @JvmStatic
    fun backgroundColor(context: Context): Int =
        themeBridge(context).semanticColors.surfacePrimary

    @JvmStatic
    fun windowBackgroundColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceWindow

    @JvmStatic
    fun cardColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceCard

    @JvmStatic
    fun floorColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceFloor

    @JvmStatic
    fun chipColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceChip

    @JvmStatic
    fun scrimColor(context: Context): Int =
        themeBridge(context).semanticColors.surfaceScrim

    @JvmStatic
    fun dividerColor(context: Context): Int =
        themeBridge(context).semanticColors.outlineLow

    @JvmStatic
    fun indicatorColor(context: Context): Int =
        themeBridge(context).semanticColors.stateIndicator

    @JvmStatic
    fun placeholderColor(context: Context): Int =
        themeBridge(context).semanticColors.decorPlaceholder

    @JvmStatic
    fun shadowColor(context: Context): Int =
        themeBridge(context).semanticColors.decorShadow

    @JvmStatic
    fun unselectedColor(context: Context): Int =
        themeBridge(context).semanticColors.stateUnselected

    @JvmStatic
    fun stateActiveColor(context: Context): Int =
        themeBridge(context).semanticColors.stateActive

    @JvmStatic
    fun outlineSurfaceColor(context: Context): Int =
        themeBridge(context).semanticColors.outlineSurface

    @JvmStatic
    fun outlineSecondaryColor(context: Context): Int =
        themeBridge(context).semanticColors.outlineSecondary

    @JvmStatic
    fun rippleColor(context: Context): Int =
        themeBridge(context).colorByAttr(context, R.attr.colorControlHighlight)

    @JvmStatic
    fun onPrimaryTextColor(context: Context): Int =
        colorOf(context) { it.onPrimary }

    @JvmStatic
    fun inverseContentColor(context: Context): Int =
        themeBridge(context).semanticColors.contentInverse
}
