package com.huanchengfly.tieba.post.ui.common.theme

import androidx.annotation.AttrRes
import com.huanchengfly.tieba.post.R

internal object ThemeDefaults {
    fun resolveAttr(@AttrRes attrId: Int): Int = when (attrId) {
        R.attr.colorPrimary -> R.color.default_color_primary
        R.attr.colorNewPrimary -> R.color.default_color_primary
        R.attr.colorAccent -> R.color.default_color_accent
        R.attr.colorOnAccent -> R.color.default_color_on_accent
        R.attr.colorToolbar -> R.color.default_color_toolbar
        R.attr.colorToolbarItem -> R.color.default_color_toolbar_item
        R.attr.colorToolbarItemSecondary -> R.color.default_color_toolbar_item_secondary
        R.attr.colorToolbarItemActive -> R.color.default_color_toolbar_item_active
        R.attr.colorToolbarSurface -> R.color.default_color_toolbar_bar
        R.attr.colorOnToolbarSurface -> R.color.default_color_on_toolbar_bar
        R.attr.colorText -> R.color.default_color_text
        R.attr.colorTextSecondary -> R.color.default_color_text_secondary
        R.attr.colorTextOnPrimary -> R.color.default_color_text_on_primary
        R.attr.color_text_disabled -> R.color.default_color_text_disabled
        R.attr.colorBackground -> R.color.default_color_background
        R.attr.colorWindowBackground -> R.color.default_color_window_background
        R.attr.colorChip -> R.color.default_color_chip
        R.attr.colorOnChip -> R.color.default_color_on_chip
        R.attr.colorUnselected -> R.color.default_color_unselected
        R.attr.colorNavBar -> R.color.default_color_nav
        R.attr.colorNavBarSurface -> R.color.default_color_nav_bar_surface
        R.attr.colorOnNavBarSurface -> R.color.default_color_on_nav_bar_surface
        R.attr.colorCard -> R.color.default_color_card
        R.attr.colorFloorCard -> R.color.default_color_floor_card
        R.attr.colorDivider -> R.color.default_color_divider
        R.attr.shadow_color -> R.color.default_color_shadow
        R.attr.colorIndicator -> R.color.default_color_swipe_refresh_view_background
        R.attr.colorPlaceholder -> R.color.default_color_placeholder
        else -> R.color.transparent
    }
}
