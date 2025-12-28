package com.huanchengfly.tieba.core.ui.locals

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
typealias OriginThreadRenderer = @Composable (
    originThreadPayload: ImmutableHolder<*>,
    modifier: Modifier,
    onClick: () -> Unit
) -> Unit

val LocalOriginThreadRenderer = staticCompositionLocalOf<OriginThreadRenderer> {
    { _, _, _ -> }
}
