package com.huanchengfly.tieba.post.ui.page.main.navigation.items

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.runtime.Composable
import com.huanchengfly.tieba.post.ui.page.main.navigation.compose.NavigationItemModel

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun NavigationItem.toNavigationModel(): NavigationItemModel =
    NavigationItemModel(
        id = id,
        iconPainter = { selected ->
            rememberAnimatedVectorPainter(
                animatedImageVector = icon(),
                atEnd = selected
            )
        },
        title = title(false),
        badgeText = if (badge) badgeText else null,
        onClick = null
    )
