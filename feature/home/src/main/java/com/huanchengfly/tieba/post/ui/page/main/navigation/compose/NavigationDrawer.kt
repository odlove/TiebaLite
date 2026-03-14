package com.huanchengfly.tieba.post.ui.page.main.navigation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.theme2.compose.Theme2Theme

@Composable
fun ThemeDrawerSheet(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Theme2Theme.colors.surfaceNav,
        contentColor = Theme2Theme.colors.stateUnselected,
        elevation = 0.dp
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ThemeNavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val activeColor = if (selected) Theme2Theme.colors.stateActive else Theme2Theme.colors.stateUnselected
    val containerColor = if (selected) activeColor.copy(alpha = 0.12f) else Theme2Theme.colors.surfaceNav
    Surface(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .fillMaxWidth(),
        onClick = onClick,
        color = containerColor,
        shape = shape,
    ) {
        CompositionLocalProvider(LocalContentColor provides activeColor) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    label()
                    if (badge != null) {
                        badge()
                    }
                }
            }
        }
    }
}
