package com.huanchengfly.tieba.core.ui.photoview

import androidx.compose.runtime.staticCompositionLocalOf

val LocalPhotoViewer = staticCompositionLocalOf<(PhotoViewData) -> Unit> {
    {}
}
