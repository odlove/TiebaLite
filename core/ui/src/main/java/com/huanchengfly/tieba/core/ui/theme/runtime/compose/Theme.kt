package com.huanchengfly.tieba.core.ui.theme.runtime.compose

import android.graphics.Color as AndroidColor
import android.util.Log
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens

const val THEME_DIAGNOSTICS_TAG = "ThemeDiagnostics"

@Stable
data class ExtendedColors(
    val theme: String,
    val isNightMode: Boolean,
    val isTranslucent: Boolean,
    val useDynamicColor: Boolean,
    val primary: Color = Color.Unspecified,
    val onPrimary: Color = Color.Unspecified,
    val accent: Color = Color.Unspecified,
    val onAccent: Color = Color.Unspecified,
    val topBar: Color = Color.Unspecified,
    val onTopBar: Color = Color.Unspecified,
    val onTopBarSecondary: Color = Color.Unspecified,
    val onTopBarActive: Color = Color.Unspecified,
    val topBarSurface: Color = Color.Unspecified,
    val onTopBarSurface: Color = Color.Unspecified,
    val bottomBar: Color = Color.Unspecified,
    val bottomBarSurface: Color = Color.Unspecified,
    val onBottomBarSurface: Color = Color.Unspecified,
    val text: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val textDisabled: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val chip: Color = Color.Unspecified,
    val onChip: Color = Color.Unspecified,
    val unselected: Color = Color.Unspecified,
    val card: Color = Color.Unspecified,
    val floorCard: Color = Color.Unspecified,
    val divider: Color = Color.Unspecified,
    val shadow: Color = Color.Unspecified,
    val indicator: Color = Color.Unspecified,
    val windowBackground: Color = Color.Unspecified,
    val placeholder: Color = Color.Unspecified,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        ThemeTokens.THEME_DEFAULT,
        isNightMode = false,
        isTranslucent = false,
        useDynamicColor = false,
    )
}

fun getColorPalette(
    darkTheme: Boolean,
    extendedColors: ExtendedColors
): Colors {
    val primaryColor = extendedColors.accent
    val secondaryColor = extendedColors.primary
    return if (darkTheme) {
        darkColors(
            primary = primaryColor,
            primaryVariant = primaryColor.darken(),
            secondary = secondaryColor,
            secondaryVariant = Color(0xFF3F310A),
            onSecondary = Color(0xFFFFFFFF),
            background = extendedColors.background,
            onBackground = extendedColors.text,
            surface = extendedColors.card,
        )
    } else {
        lightColors(
            primary = primaryColor,
            primaryVariant = primaryColor.darken(),
            secondary = secondaryColor,
            secondaryVariant = Color(0xFF000000),
            onSecondary = Color(0xFFFFFFFF),
            background = extendedColors.background,
            onBackground = extendedColors.text,
            surface = extendedColors.card,
        )
    }
}

private fun Color.darken(amount: Float = 0.1f): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(this.toArgb(), hsv)
    hsv[2] = (hsv[2] * (1f - amount)).coerceIn(0f, 1f)
    val alphaInt = (this.alpha * 255).toInt().coerceIn(0, 255)
    val argb = AndroidColor.HSVToColor(alphaInt, hsv)
    return Color(argb)
}

private fun ThemeState.toExtendedColors(): ExtendedColors {
    val palette = palette
    return ExtendedColors(
        theme = resolvedTheme,
        isNightMode = isNightMode,
        isTranslucent = isTranslucent,
        useDynamicColor = useDynamicColor,
        primary = palette.primaryAlt.toColor(),
        onPrimary = palette.textOnPrimary.toColor(),
        accent = palette.accent.toColor(),
        onAccent = palette.onAccent.toColor(),
        topBar = palette.toolbar.toColor(),
        onTopBar = palette.toolbarItem.toColor(),
        onTopBarSecondary = palette.toolbarItemSecondary.toColor(),
        onTopBarActive = palette.toolbarItemActive.toColor(),
        topBarSurface = palette.toolbarSurface.toColor(),
        onTopBarSurface = palette.onToolbarSurface.toColor(),
        bottomBar = palette.navBar.toColor(),
        bottomBarSurface = palette.navBarSurface.toColor(),
        onBottomBarSurface = palette.onNavBarSurface.toColor(),
        text = palette.textPrimary.toColor(),
        textSecondary = palette.textSecondary.toColor(),
        textDisabled = palette.textDisabled.toColor(),
        background = palette.background.toColor(),
        chip = palette.chip.toColor(),
        onChip = palette.onChip.toColor(),
        unselected = palette.unselected.toColor(),
        card = palette.card.toColor(),
        floorCard = palette.floorCard.toColor(),
        divider = palette.divider.toColor(),
        shadow = palette.shadow.toColor(),
        indicator = palette.indicator.toColor(),
        windowBackground = palette.windowBackground.toColor(),
        placeholder = palette.placeholder.toColor(),
    )
}

private fun Int.toColor(): Color = Color(this)

@Composable
fun TiebaLiteTheme(
    content: @Composable () -> Unit
) {
    val themeState = LocalThemeState.current
    val extendedColors = remember(themeState) { themeState.toExtendedColors() }
    val isDarkColorPalette = themeState.isNightMode ||
        (themeState.isTranslucent && themeState.effectiveTheme.contains("dark", ignoreCase = true))

    SideEffect {
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "TiebaLiteTheme composited raw=${themeState.rawTheme} " +
                "effective=${themeState.effectiveTheme} resolved=${themeState.resolvedTheme} " +
                "translucent=${themeState.isTranslucent} dynamic=${themeState.useDynamicColor}"
        )
    }

    val colors = getColorPalette(isDarkColorPalette, extendedColors)

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
