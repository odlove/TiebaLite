package com.huanchengfly.tieba.post.ui.common.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.compose.ui.graphics.toArgb
import com.huanchengfly.tieba.core.ui.theme.ThemeSwitcher
import com.huanchengfly.tieba.core.ui.theme.ThemeUtils
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.dynamicTonalPalette
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.getColorCompat
import com.huanchengfly.tieba.post.utils.Util

object DefaultThemeDelegate : ThemeSwitcher {
    override fun getColorByAttr(context: Context, attrId: Int): Int {
        return getColorByAttr(context, attrId, ThemeUtil.getCurrentTheme(checkDynamic = true))
    }

    @SuppressLint("DiscouragedApi")
    fun getColorByAttr(context: Context, @AttrRes attrId: Int, theme: String): Int {
        if (!com.huanchengfly.tieba.post.App.isInitialized) {
            return context.getColorCompat(ThemeDefaults.resolveAttr(attrId))
        }
        val resources = context.resources
        return when (attrId) {
            R.attr.colorPrimary -> {
                if (ThemeUtil.isDynamicTheme(theme) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val dynamicTonalPalette = dynamicTonalPalette(context)
                    if (ThemeUtil.isNightMode(theme)) {
                        dynamicTonalPalette.primary80.toArgb()
                    } else {
                        dynamicTonalPalette.primary40.toArgb()
                    }
                } else if (ThemeUtil.THEME_CUSTOM == theme) {
                    val customPrimaryColorStr = context.appPreferences.customPrimaryColor
                    if (customPrimaryColorStr != null) {
                        Color.parseColor(customPrimaryColorStr)
                    } else getColorByAttr(context, attrId, ThemeUtil.THEME_DEFAULT)
                } else if (ThemeUtil.isTranslucentTheme(theme)) {
                    val primaryColorStr = context.appPreferences.translucentPrimaryColor
                    if (primaryColorStr != null) {
                        Color.parseColor(primaryColorStr)
                    } else getColorByAttr(context, attrId, ThemeUtil.THEME_DEFAULT)
                } else {
                    context.getColorCompat(
                        resources.getIdentifier(
                            "theme_color_primary_$theme",
                            "color",
                            context.packageName
                        )
                    )
                }
            }
            R.attr.colorNewPrimary -> {
                if (ThemeUtil.isDynamicTheme(theme) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val dynamicTonalPalette = dynamicTonalPalette(context)
                    if (ThemeUtil.isNightMode(theme)) dynamicTonalPalette.primary80.toArgb() else dynamicTonalPalette.primary40.toArgb()
                } else if (ThemeUtil.isNightMode(theme)) {
                    context.getColorCompat(R.color.theme_color_new_primary_night)
                } else if (theme == ThemeUtil.THEME_DEFAULT) {
                    context.getColorCompat(R.color.theme_color_new_primary_light)
                } else {
                    getColorByAttr(context, R.attr.colorPrimary, theme)
                }
            }
            R.attr.colorAccent -> {
                if (ThemeUtil.isDynamicTheme(theme) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val dynamicTonalPalette = dynamicTonalPalette(context)
                    if (ThemeUtil.isNightMode(theme)) dynamicTonalPalette.secondary80.toArgb() else dynamicTonalPalette.secondary40.toArgb()
                } else if (ThemeUtil.THEME_CUSTOM == theme || ThemeUtil.isTranslucentTheme(theme)) {
                    getColorByAttr(context, R.attr.colorPrimary, theme)
                } else {
                    context.getColorCompat(
                        resources.getIdentifier(
                            "theme_color_accent_$theme",
                            "color",
                            context.packageName
                        )
                    )
                }
            }
            R.attr.colorOnAccent -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_on_accent_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_on_accent_light)
            }
            R.attr.colorToolbar -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(resources.getIdentifier("theme_color_toolbar_$theme", "color", context.packageName))
                } else {
                    val isPrimaryColor = context.appPreferences.toolbarPrimaryColor
                    if (isPrimaryColor) {
                        getColorByAttr(context, R.attr.colorPrimary, theme)
                    } else {
                        context.getColorCompat(R.color.white)
                    }
                }
            }
            R.attr.colorText -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(resources.getIdentifier("color_text_$theme", "color", context.packageName))
                } else context.getColorCompat(if (ThemeUtil.isNightMode(theme)) R.color.color_text_night else R.color.color_text)
            }
            R.attr.color_text_disabled -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(resources.getIdentifier("color_text_disabled_$theme", "color", context.packageName))
                } else context.getColorCompat(if (ThemeUtil.isNightMode(theme)) R.color.color_text_disabled_night else R.color.color_text_disabled)
            }
            R.attr.colorTextSecondary -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(resources.getIdentifier("color_text_secondary_$theme", "color", context.packageName))
                } else context.getColorCompat(if (ThemeUtil.isNightMode(theme)) R.color.color_text_secondary_night else R.color.color_text_secondary)
            }
            R.attr.colorTextOnPrimary -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(R.color.white)
                } else getColorByAttr(context, R.attr.colorBackground, theme)
            }
            R.attr.colorBackground -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(R.color.transparent)
                } else if (ThemeUtil.isNightMode(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_background_$theme", "color", context.packageName)
                    )
                } else {
                    context.getColorCompat(R.color.theme_color_background_light)
                }
            }
            R.attr.colorWindowBackground -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_window_background_$theme", "color", context.packageName)
                    )
                } else if (ThemeUtil.isNightMode()) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_background_$theme", "color", context.packageName)
                    )
                } else {
                    context.getColorCompat(R.color.theme_color_background_light)
                }
            }
            R.attr.colorChip -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_chip_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_chip_light)
            }
            R.attr.colorOnChip -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_on_chip_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_on_chip_light)
            }
            R.attr.colorUnselected -> {
                context.getColorCompat(
                    if (ThemeUtil.isNightMode(theme)) resources.getIdentifier(
                        "theme_color_unselected_$theme",
                        "color",
                        context.packageName
                    ) else R.color.theme_color_unselected_day
                )
            }
            R.attr.colorNavBar -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(R.color.transparent)
                } else if (ThemeUtil.isNightMode(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_nav_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_nav_light)
            }
            R.attr.colorFloorCard -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_floor_card_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_floor_card_light)
            }
            R.attr.colorCard -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_card_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_card_light)
            }
            R.attr.colorDivider -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_divider_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_divider_light)
            }
            R.attr.shadow_color -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(R.color.transparent)
                } else context.getColorCompat(if (ThemeUtil.isNightMode(theme)) R.color.theme_color_shadow_night else R.color.theme_color_shadow_day)
            }
            R.attr.colorToolbarItem -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_toolbar_item_$theme", "color", context.packageName)
                    )
                } else if (ThemeUtil.isNightMode(theme)) {
                    context.getColorCompat(R.color.theme_color_toolbar_item_night)
                } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark()) R.color.theme_color_toolbar_item_light else R.color.theme_color_toolbar_item_dark)
            }
            R.attr.colorToolbarItemActive -> {
                if (ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_toolbar_item_active_$theme", "color", context.packageName)
                    )
                } else if (ThemeUtil.isNightMode(theme)) {
                    getColorByAttr(context, R.attr.colorAccent, theme)
                } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark()) R.color.theme_color_toolbar_item_light else R.color.theme_color_toolbar_item_dark)
            }
            R.attr.colorToolbarItemSecondary -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_toolbar_item_secondary_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark()) R.color.theme_color_toolbar_item_secondary_white else R.color.theme_color_toolbar_item_secondary_light)
            }
            R.attr.colorToolbarSurface -> {
                if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_toolbar_surface_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_toolbar_surface_light)
            }
            R.attr.colorOnToolbarSurface -> {
                if (ThemeUtil.isTranslucentTheme(theme) || ThemeUtil.isNightMode(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_on_toolbar_surface_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_on_toolbar_surface_light)
            }
            R.attr.colorNavBarSurface -> {
                if (ThemeUtil.isNightMode(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_nav_bar_surface_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_nav_bar_surface_light)
            }
            R.attr.colorOnNavBarSurface -> {
                if (ThemeUtil.isNightMode(theme)) {
                    context.getColorCompat(R.color.theme_color_on_nav_bar_surface_dark)
                } else context.getColorCompat(R.color.theme_color_on_nav_bar_surface_light)
            }
            R.attr.colorPlaceholder -> {
                if (ThemeUtil.isTranslucentTheme(theme) || ThemeUtil.isNightMode(theme)) {
                    context.getColorCompat(
                        resources.getIdentifier("theme_color_placeholder_$theme", "color", context.packageName)
                    )
                } else context.getColorCompat(R.color.theme_color_placeholder_light)
            }
            R.attr.colorIndicator -> {
                context.getColorCompat(
                    if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        resources.getIdentifier("theme_color_indicator_$theme", "color", context.packageName)
                    } else R.color.default_color_swipe_refresh_view_background
                )
            }
            else -> context.getColorCompat(ThemeDefaults.resolveAttr(attrId))
        }
    }

    @ColorInt
    override fun getColorById(context: Context, @ColorRes colorId: Int): Int {
        return when (colorId) {
            R.color.default_color_primary -> getColorByAttr(context, R.attr.colorPrimary)
            R.color.default_color_accent -> getColorByAttr(context, R.attr.colorAccent)
            R.color.default_color_on_accent -> getColorByAttr(context, R.attr.colorOnAccent)
            R.color.default_color_chip -> getColorByAttr(context, R.attr.colorChip)
            R.color.default_color_background -> getColorByAttr(context, R.attr.colorBackground)
            R.color.default_color_window_background -> getColorByAttr(context, R.attr.colorWindowBackground)
            R.color.default_color_toolbar -> getColorByAttr(context, R.attr.colorToolbar)
            R.color.default_color_toolbar_item -> getColorByAttr(context, R.attr.colorToolbarItem)
            R.color.default_color_toolbar_item_active -> getColorByAttr(context, R.attr.colorToolbarItemActive)
            R.color.default_color_toolbar_item_secondary -> getColorByAttr(context, R.attr.colorToolbarItemSecondary)
            R.color.default_color_toolbar_bar -> getColorByAttr(context, R.attr.colorToolbarSurface)
            R.color.default_color_on_toolbar_bar -> getColorByAttr(context, R.attr.colorOnToolbarSurface)
            R.color.default_color_nav_bar_surface -> getColorByAttr(context, R.attr.colorNavBarSurface)
            R.color.default_color_on_nav_bar_surface -> getColorByAttr(context, R.attr.colorOnNavBarSurface)
            R.color.default_color_card -> getColorByAttr(context, R.attr.colorCard)
            R.color.default_color_floor_card -> getColorByAttr(context, R.attr.colorFloorCard)
            R.color.default_color_nav -> getColorByAttr(context, R.attr.colorNavBar)
            R.color.default_color_shadow -> getColorByAttr(context, R.attr.shadow_color)
            R.color.default_color_unselected -> getColorByAttr(context, R.attr.colorUnselected)
            R.color.default_color_text -> getColorByAttr(context, R.attr.colorText)
            R.color.default_color_text_on_primary -> getColorByAttr(context, R.attr.colorTextOnPrimary)
            R.color.default_color_text_secondary -> getColorByAttr(context, R.attr.colorTextSecondary)
            R.color.default_color_text_disabled -> getColorByAttr(context, R.attr.color_text_disabled)
            R.color.default_color_divider -> getColorByAttr(context, R.attr.colorDivider)
            R.color.default_color_swipe_refresh_view_background -> getColorByAttr(context, R.attr.colorIndicator)
            else -> context.getColorCompat(colorId)
        }
    }
}
