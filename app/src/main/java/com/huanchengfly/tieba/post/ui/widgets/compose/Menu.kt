package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.menuBackground

typealias MenuScope = com.huanchengfly.tieba.core.ui.compose.MenuScope
typealias MenuState = com.huanchengfly.tieba.core.ui.compose.MenuState

@Composable
fun rememberMenuState(): MenuState = com.huanchengfly.tieba.core.ui.compose.rememberMenuState()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickMenu(
    menuContent: @Composable MenuScope.() -> Unit,
    modifier: Modifier = Modifier,
    menuState: MenuState = rememberMenuState(),
    menuShape: Shape = RoundedCornerShape(14.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = LocalIndication.current,
    triggerShape: Shape? = null,
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    com.huanchengfly.tieba.core.ui.compose.ClickMenu(
        menuContent = menuContent,
        modifier = modifier,
        menuState = menuState,
        menuShape = menuShape,
        interactionSource = interactionSource,
        indication = indication,
        triggerShape = triggerShape,
        onDismiss = onDismiss,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = ExtendedTheme.colors.text,
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongClickMenu(
    menuContent: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuState: MenuState = rememberMenuState(),
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(0.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = LocalIndication.current,
    content: @Composable () -> Unit,
) {
    com.huanchengfly.tieba.core.ui.compose.LongClickMenu(
        menuContent = menuContent,
        modifier = modifier,
        enabled = enabled,
        menuState = menuState,
        onClick = onClick,
        shape = shape,
        interactionSource = interactionSource,
        indication = indication,
        backgroundColor = ExtendedTheme.colors.menuBackground,
        contentColor = ExtendedTheme.colors.text,
        content = content
    )
}
