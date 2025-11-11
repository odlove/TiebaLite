package com.huanchengfly.tieba.core.ui.locals

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo

typealias OriginThreadRenderer = @Composable (
    originThreadInfo: ImmutableHolder<OriginThreadInfo>,
    modifier: Modifier,
    onClick: () -> Unit
) -> Unit

val LocalOriginThreadRenderer = staticCompositionLocalOf<OriginThreadRenderer> {
    { _, _, _ -> }
}
