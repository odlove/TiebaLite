package com.huanchengfly.tieba.core.ui.media.photoview

import androidx.compose.runtime.staticCompositionLocalOf

val LocalPhotoViewer = staticCompositionLocalOf<(PhotoViewData) -> Unit> {
    {}
}
