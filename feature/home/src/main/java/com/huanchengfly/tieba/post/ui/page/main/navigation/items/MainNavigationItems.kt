package com.huanchengfly.tieba.post.ui.page.main.navigation.items

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.feature.home.R as HomeR
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.ui.ExplorePage
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.HomePage
import com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.ui.NotificationsPage
import com.huanchengfly.tieba.post.ui.page.main.tabs.user.ui.UserPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
internal fun rememberMainNavigationItems(
    messageCount: Int,
    canOpenExplore: Boolean,
    onOpenExplore: () -> Unit,
    onNotificationsClick: () -> Unit,
): ImmutableList<NavigationItem> {
    val navigationItems by remember(
        messageCount,
        canOpenExplore,
        onOpenExplore,
        onNotificationsClick
    ) {
        derivedStateOf {
            listOfNotNull(
                NavigationItem(
                    id = "home",
                    icon = {
                        AnimatedImageVector.animatedVectorResource(
                            id = HomeR.drawable.ic_animated_rounded_inventory_2
                        )
                    },
                    title = { stringResource(id = CoreUiR.string.title_main) },
                    content = {
                        HomePage(canOpenExplore = canOpenExplore) {
                            onOpenExplore()
                        }
                    }
                ),
                NavigationItem(
                    id = "explore",
                    icon = {
                        AnimatedImageVector.animatedVectorResource(
                            id = HomeR.drawable.ic_animated_toy_fans
                        )
                    },
                    title = { stringResource(id = CoreUiR.string.title_explore) },
                    content = {
                        ExplorePage()
                    }
                ),
                NavigationItem(
                    id = "notification",
                    icon = {
                        AnimatedImageVector.animatedVectorResource(
                            id = HomeR.drawable.ic_animated_rounded_notifications
                        )
                    },
                    title = { stringResource(id = CoreUiR.string.title_notifications) },
                    badge = messageCount > 0,
                    badgeText = "$messageCount",
                    onClick = onNotificationsClick,
                    content = {
                        NotificationsPage()
                    }
                ),
                NavigationItem(
                    id = "user",
                    icon = {
                        AnimatedImageVector.animatedVectorResource(
                            id = HomeR.drawable.ic_animated_rounded_person
                        )
                    },
                    title = { stringResource(id = CoreUiR.string.title_user) },
                    content = {
                        UserPage()
                    }
                ),
            ).toImmutableList()
        }
    }
    return navigationItems
}
