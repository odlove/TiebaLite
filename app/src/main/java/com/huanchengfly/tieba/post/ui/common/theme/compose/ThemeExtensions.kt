package com.huanchengfly.tieba.post.ui.common.theme.compose

import androidx.compose.ui.graphics.Color

val ExtendedColors.pullRefreshIndicator: Color
    get() = if (isTranslucent) {
        windowBackground
    } else {
        indicator
    }

val ExtendedColors.loadMoreIndicator: Color
    get() = if (isTranslucent) {
        windowBackground
    } else {
        indicator
    }

val ExtendedColors.threadBottomBar: Color
    get() = if (isTranslucent) {
        windowBackground
    } else {
        bottomBar
    }

val ExtendedColors.menuBackground: Color
    get() = if (isTranslucent) {
        windowBackground
    } else {
        card
    }

val ExtendedColors.invertChipBackground: Color
    get() = if (isNightMode) primary.copy(alpha = 0.3f) else primary

val ExtendedColors.invertChipContent: Color
    get() = if (isNightMode) primary else onPrimary
