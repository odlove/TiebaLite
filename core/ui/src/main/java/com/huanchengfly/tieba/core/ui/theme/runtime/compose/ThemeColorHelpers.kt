package com.huanchengfly.tieba.core.ui.theme.runtime.compose

import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 提供统一的 Compose 颜色 helper，避免在各页面重复判断 onTopBar/text。
 */

fun ExtendedColors.resolveTopBarContentColor(): Color = onTopBar

fun ExtendedColors.resolveTopBarSecondaryColor(): Color = onTopBarSecondary

@Composable
fun topBarContentColor(): Color = ExtendedTheme.colors.resolveTopBarContentColor()

@Composable
fun topBarSecondaryColor(): Color = ExtendedTheme.colors.resolveTopBarSecondaryColor()

@Composable
fun topBarSubtitleColor(): Color = topBarSecondaryColor()

@Composable
fun topBarIconTint(): Color = ExtendedTheme.colors.onTopBar

@Composable
fun tabSelectedColor(): Color {
    return ExtendedTheme.colors.onTopBar
}

@Composable
fun tabUnselectedColor(): Color {
    return ExtendedTheme.colors.onTopBarSecondary
}

@Composable
fun tabIndicatorColor(): Color = ExtendedTheme.colors.indicator

@Composable
fun navigationSelectedColor(): Color = ExtendedTheme.colors.accent

@Composable
fun navigationUnselectedColor(): Color = ExtendedTheme.colors.unselected

@Composable
fun searchBoxBackgroundColor(): Color = ExtendedTheme.colors.chip

@Composable
fun searchBoxContentColor(): Color = ExtendedTheme.colors.text

@Composable
fun cardBackgroundColor(): Color = ExtendedTheme.colors.card

@Composable
fun cardContentColor(): Color = ExtendedTheme.colors.text

@Composable
fun cardSecondaryContentColor(): Color = ExtendedTheme.colors.textSecondary

@Composable
fun settingsTitleColor(): Color = ExtendedTheme.colors.text

@Composable
fun settingsSubtitleColor(): Color = ExtendedTheme.colors.textSecondary

@Composable
fun settingsIconTint(): Color = ExtendedTheme.colors.onChip

@Composable
fun dialogBackgroundColor(): Color = ExtendedTheme.colors.menuBackground

@Composable
fun dialogContentColor(): Color = ExtendedTheme.colors.text

@Composable
fun dialogPrimaryButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        backgroundColor = ExtendedTheme.colors.accent,
        contentColor = ExtendedTheme.colors.onAccent,
        disabledBackgroundColor = ExtendedTheme.colors.accent.copy(alpha = 0.5f),
        disabledContentColor = ExtendedTheme.colors.onAccent.copy(alpha = ContentAlpha.disabled),
    )

@Composable
fun dialogSecondaryButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        backgroundColor = ExtendedTheme.colors.textSecondary.copy(alpha = 0.12f),
        contentColor = ExtendedTheme.colors.textSecondary,
        disabledBackgroundColor = ExtendedTheme.colors.textSecondary.copy(alpha = 0.12f),
        disabledContentColor = ExtendedTheme.colors.textSecondary.copy(alpha = ContentAlpha.disabled),
    )

@Composable
fun sheetHandleColor(handleAlpha: Float = 0.5f): Color =
    ExtendedTheme.colors.onTopBarSurface.copy(alpha = handleAlpha)
