package com.huanchengfly.tieba.core.ui.theme.runtime

import androidx.annotation.AttrRes
import com.huanchengfly.tieba.core.ui.R

internal object ThemeDefaults {
    fun resolveAttr(@AttrRes attrId: Int): Int = when (attrId) {
        R.attr.colorPrimary -> R.color.sem_state_active
        R.attr.colorNewPrimary -> R.color.sem_state_active
        R.attr.colorAccent -> R.color.sem_state_active
        R.attr.colorOnAccent -> R.color.sem_content_on_primary
        R.attr.colorToolbar -> R.color.sem_surface_toolbar
        R.attr.colorToolbarItem -> R.color.sem_content_primary
        R.attr.colorToolbarItemSecondary -> R.color.sem_content_secondary
        R.attr.colorToolbarItemActive -> R.color.sem_state_active
        R.attr.colorToolbarSurface -> R.color.sem_surface_toolbar
        R.attr.colorOnToolbarSurface -> R.color.sem_outline_surface
        R.attr.colorText -> R.color.sem_content_primary
        R.attr.colorTextSecondary -> R.color.sem_content_secondary
        R.attr.colorTextOnPrimary -> R.color.sem_content_on_primary
        R.attr.color_text_disabled -> R.color.sem_content_disabled
        R.attr.colorBackground -> R.color.sem_surface_primary
        R.attr.colorWindowBackground -> R.color.sem_surface_window
        R.attr.colorChip -> R.color.sem_surface_chip
        R.attr.colorOnChip -> R.color.sem_content_on_chip
        R.attr.colorUnselected -> R.color.sem_state_unselected
        R.attr.colorNavBar -> R.color.sem_surface_nav
        R.attr.colorNavBarSurface -> R.color.sem_surface_nav_surface
        R.attr.colorOnNavBarSurface -> R.color.sem_content_secondary
        R.attr.colorCard -> R.color.sem_surface_card
        R.attr.colorFloorCard -> R.color.sem_surface_floor
        R.attr.colorDivider -> R.color.sem_outline_low
        R.attr.shadow_color -> R.color.sem_decor_shadow
        R.attr.colorIndicator -> R.color.sem_state_indicator
        R.attr.colorPlaceholder -> R.color.sem_decor_placeholder
        else -> R.color.transparent
    }
}
