package com.huanchengfly.tieba.core.ui.theme.runtime

import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import com.huanchengfly.tieba.core.ui.theme.ThemePalette
import com.huanchengfly.tieba.core.ui.R

object ThemePaletteMapper {
    fun colorForAttr(palette: ThemePalette, @AttrRes attrId: Int): Int? = when (attrId) {
        R.attr.colorPrimary -> palette.primary
        R.attr.colorNewPrimary -> palette.primaryAlt
        R.attr.colorAccent -> palette.accent
        R.attr.colorOnAccent -> palette.onAccent
        R.attr.colorToolbar -> palette.toolbar
        R.attr.colorToolbarItem -> palette.toolbarItem
        R.attr.colorToolbarItemSecondary -> palette.toolbarItemSecondary
        R.attr.colorToolbarItemActive -> palette.toolbarItemActive
        R.attr.colorToolbarSurface -> palette.toolbarSurface
        R.attr.colorOnToolbarSurface -> palette.onToolbarSurface
        R.attr.colorNavBar -> palette.navBar
        R.attr.colorNavBarSurface -> palette.navBarSurface
        R.attr.colorOnNavBarSurface -> palette.onNavBarSurface
        R.attr.colorText -> palette.textPrimary
        R.attr.colorTextSecondary -> palette.textSecondary
        R.attr.color_text_disabled -> palette.textDisabled
        R.attr.colorTextOnPrimary -> palette.textOnPrimary
        R.attr.colorBackground -> palette.background
        R.attr.colorWindowBackground -> palette.windowBackground
        R.attr.colorChip -> palette.chip
        R.attr.colorOnChip -> palette.onChip
        R.attr.colorUnselected -> palette.unselected
        R.attr.colorFloorCard -> palette.floorCard
        R.attr.colorCard -> palette.card
        R.attr.colorDivider -> palette.divider
        R.attr.colorIndicator -> palette.indicator
        R.attr.colorPlaceholder -> palette.placeholder
        R.attr.shadow_color -> palette.shadow
        else -> null
    }

    fun colorForRes(palette: ThemePalette, @ColorRes colorId: Int): Int? = when (colorId) {
        R.color.theme_color_primary_tieba,
        R.color.theme_color_primary_blue,
        R.color.theme_color_primary_pink,
        R.color.theme_color_primary_red,
        R.color.theme_color_primary_purple,
        R.color.theme_color_primary_black,
        R.color.theme_color_primary_dark,
        R.color.theme_color_primary_blue_dark,
        R.color.theme_color_primary_grey_dark,
        R.color.theme_color_primary_amoled_dark -> palette.primary

        R.color.theme_color_new_primary_light,
        R.color.theme_color_new_primary_night -> palette.primaryAlt

        R.color.theme_color_accent_tieba,
        R.color.theme_color_accent_blue,
        R.color.theme_color_accent_pink,
        R.color.theme_color_accent_red,
        R.color.theme_color_accent_purple,
        R.color.theme_color_accent_black,
        R.color.theme_color_accent_dark,
        R.color.theme_color_accent_blue_dark,
        R.color.theme_color_accent_grey_dark,
        R.color.theme_color_accent_amoled_dark -> palette.accent

        R.color.theme_color_on_accent_light,
        R.color.theme_color_on_accent_dark,
        R.color.theme_color_on_accent_translucent,
        R.color.theme_color_on_accent_translucent_light,
        R.color.theme_color_on_accent_translucent_dark -> palette.onAccent

        R.color.theme_color_toolbar_surface_light,
        R.color.theme_color_toolbar_surface_dark,
        R.color.theme_color_toolbar_surface_translucent,
        R.color.theme_color_toolbar_surface_translucent_light,
        R.color.theme_color_toolbar_surface_translucent_dark,
        R.color.theme_color_toolbar_surface_blue_dark,
        R.color.theme_color_toolbar_surface_grey_dark,
        R.color.theme_color_toolbar_surface_amoled_dark -> palette.toolbarSurface

        R.color.theme_color_toolbar_item_dark,
        R.color.theme_color_toolbar_item_light,
        R.color.theme_color_toolbar_item_translucent,
        R.color.theme_color_toolbar_item_translucent_light,
        R.color.theme_color_toolbar_item_translucent_dark -> palette.toolbarItem

        R.color.theme_color_toolbar_item_secondary_light,
        R.color.theme_color_toolbar_item_secondary_white,
        R.color.theme_color_toolbar_item_secondary_dark,
        R.color.theme_color_toolbar_item_secondary_translucent,
        R.color.theme_color_toolbar_item_secondary_translucent_light,
        R.color.theme_color_toolbar_item_secondary_translucent_dark,
        R.color.theme_color_toolbar_item_secondary_blue_dark,
        R.color.theme_color_toolbar_item_secondary_grey_dark,
        R.color.theme_color_toolbar_item_secondary_amoled_dark -> palette.toolbarItemSecondary

        R.color.theme_color_toolbar_item_active_dark,
        R.color.theme_color_toolbar_item_active_translucent,
        R.color.theme_color_toolbar_item_active_translucent_light,
        R.color.theme_color_toolbar_item_active_translucent_dark,
        R.color.theme_color_toolbar_item_active_blue_dark,
        R.color.theme_color_toolbar_item_active_grey_dark,
        R.color.theme_color_toolbar_item_active_amoled_dark -> palette.toolbarItemActive

        R.color.theme_color_background_light,
        R.color.theme_color_background_dark,
        R.color.theme_color_background_blue_dark,
        R.color.theme_color_background_grey_dark,
        R.color.theme_color_background_amoled_dark -> palette.background

        R.color.theme_color_window_background_light,
        R.color.theme_color_window_background_dark,
        R.color.theme_color_window_background_translucent,
        R.color.theme_color_window_background_translucent_light,
        R.color.theme_color_window_background_translucent_dark,
        R.color.theme_color_window_background_blue_dark,
        R.color.theme_color_window_background_grey_dark,
        R.color.theme_color_window_background_amoled_dark -> palette.windowBackground

        R.color.theme_color_card_light,
        R.color.theme_color_card_dark,
        R.color.theme_color_card_translucent,
        R.color.theme_color_card_translucent_dark,
        R.color.theme_color_card_blue_dark,
        R.color.theme_color_card_grey_dark,
        R.color.theme_color_card_amoled_dark -> palette.card

        R.color.theme_color_floor_card_light,
        R.color.theme_color_floor_card_dark,
        R.color.theme_color_floor_card_translucent,
        R.color.theme_color_floor_card_translucent_dark,
        R.color.theme_color_floor_card_blue_dark,
        R.color.theme_color_floor_card_grey_dark,
        R.color.theme_color_floor_card_amoled_dark -> palette.floorCard

        R.color.theme_color_chip_light,
        R.color.theme_color_chip_dark,
        R.color.theme_color_chip_translucent,
        R.color.theme_color_chip_translucent_light,
        R.color.theme_color_chip_translucent_dark,
        R.color.theme_color_chip_blue_dark,
        R.color.theme_color_chip_grey_dark,
        R.color.theme_color_chip_amoled_dark -> palette.chip

        R.color.theme_color_on_chip_light,
        R.color.theme_color_on_chip_dark,
        R.color.theme_color_on_chip_translucent,
        R.color.theme_color_on_chip_translucent_light,
        R.color.theme_color_on_chip_translucent_dark -> palette.onChip

        R.color.theme_color_nav_light,
        R.color.theme_color_nav_dark,
        R.color.theme_color_nav_blue_dark,
        R.color.theme_color_nav_grey_dark,
        R.color.theme_color_nav_amoled_dark -> palette.navBar

        R.color.theme_color_nav_bar_surface_light,
        R.color.theme_color_nav_bar_surface_dark,
        R.color.theme_color_nav_bar_surface_blue_dark,
        R.color.theme_color_nav_bar_surface_grey_dark,
        R.color.theme_color_nav_bar_surface_amoled_dark -> palette.navBarSurface

        R.color.theme_color_on_nav_bar_surface_light,
        R.color.theme_color_on_nav_bar_surface_dark -> palette.onNavBarSurface

        R.color.theme_color_unselected_day,
        R.color.theme_color_unselected_dark,
        R.color.theme_color_unselected_translucent,
        R.color.theme_color_unselected_translucent_light,
        R.color.theme_color_unselected_translucent_dark,
        R.color.theme_color_unselected_blue_dark,
        R.color.theme_color_unselected_grey_dark,
        R.color.theme_color_unselected_amoled_dark -> palette.unselected

        R.color.color_text,
        R.color.color_text_night,
        R.color.color_text_translucent_light,
        R.color.color_text_translucent_dark -> palette.textPrimary

        R.color.color_text_secondary,
        R.color.color_text_secondary_night,
        R.color.color_text_secondary_translucent_light,
        R.color.color_text_secondary_translucent_dark -> palette.textSecondary

        R.color.color_text_disabled,
        R.color.color_text_disabled_night,
        R.color.color_text_disabled_translucent_light,
        R.color.color_text_disabled_translucent_dark -> palette.textDisabled

        R.color.theme_color_divider_light,
        R.color.theme_color_divider_dark,
        R.color.theme_color_divider_translucent,
        R.color.theme_color_divider_translucent_light,
        R.color.theme_color_divider_translucent_dark,
        R.color.theme_color_divider_blue_dark,
        R.color.theme_color_divider_grey_dark,
        R.color.theme_color_divider_amoled_dark -> palette.divider

        R.color.theme_color_placeholder_light,
        R.color.theme_color_placeholder_dark,
        R.color.theme_color_placeholder_translucent,
        R.color.theme_color_placeholder_translucent_light,
        R.color.theme_color_placeholder_translucent_dark,
        R.color.theme_color_placeholder_blue_dark,
        R.color.theme_color_placeholder_grey_dark,
        R.color.theme_color_placeholder_amoled_dark -> palette.placeholder

        R.color.theme_color_indicator_light,
        R.color.theme_color_indicator_dark,
        R.color.theme_color_indicator_translucent,
        R.color.theme_color_indicator_translucent_light,
        R.color.theme_color_indicator_translucent_dark,
        R.color.theme_color_indicator_blue_dark,
        R.color.theme_color_indicator_grey_dark,
        R.color.theme_color_indicator_amoled_dark,
        R.color.default_color_swipe_refresh_view_background -> palette.indicator

        R.color.theme_color_shadow_day,
        R.color.theme_color_shadow_night -> palette.shadow
        else -> null
    }
}
