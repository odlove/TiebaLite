package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.ui.compose.ListMenuItem as CoreListMenuItem
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

@Composable
fun ListMenuItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconColor: Color = ExtendedTheme.colors.primary,
    textStyle: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    ),
    onClick: (() -> Unit)? = null,
    customContent: @Composable (RowScope.() -> Unit)? = null,
) {
    CoreListMenuItem(
        icon = icon,
        text = text,
        modifier = modifier,
        iconColor = iconColor,
        textStyle = textStyle,
        onClick = onClick,
        customContent = customContent
    )
}
