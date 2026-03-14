package com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.drawer

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.theme.compose.navigationSelectedColor
import com.huanchengfly.tieba.core.theme.compose.navigationUnselectedColor
import com.huanchengfly.tieba.core.theme.compose.topBarSecondaryColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val activeColor = navigationSelectedColor()
    val inactiveColor = navigationUnselectedColor()
    val inactiveSecondaryColor = topBarSecondaryColor()
    val containerColor = if (selected) activeColor.copy(alpha = 0.12f) else Color.Transparent

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .height(ActiveIndicatorHeight)
            .fillMaxWidth(),
        shape = shape,
        color = containerColor,
        interactionSource = interactionSource,
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                val iconColor = if (selected) activeColor else inactiveSecondaryColor
                CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
                Spacer(Modifier.width(12.dp))
            }
            Box(Modifier.weight(1f)) {
                val labelColor = if (selected) activeColor else inactiveColor
                CompositionLocalProvider(LocalContentColor provides labelColor, content = label)
            }
            if (badge != null) {
                Spacer(Modifier.width(12.dp))
                val badgeColor = if (selected) activeColor else inactiveSecondaryColor
                CompositionLocalProvider(LocalContentColor provides badgeColor, content = badge)
            }
        }
    }
}
