package com.huanchengfly.tieba.core.ui.widgets.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonElevation
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.ui.compose.CommonButton
import com.huanchengfly.tieba.core.ui.compose.CommonTextButton
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    shape: Shape = RoundedCornerShape(100),
    border: BorderStroke? = null,
    colors: androidx.compose.material.ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = ExtendedTheme.colors.primary,
        contentColor = ExtendedTheme.colors.onAccent
    ),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    CommonButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = RoundedCornerShape(100),
    border: BorderStroke? = null,
    color: Color = ExtendedTheme.colors.text,
    colors: androidx.compose.material.ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = color.copy(alpha = 0.1f),
        contentColor = color,
        disabledBackgroundColor = color.copy(alpha = ContentAlpha.disabled * 0.1f),
        disabledContentColor = color.copy(alpha = ContentAlpha.disabled)
    ),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    CommonTextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}
