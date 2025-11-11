package com.huanchengfly.tieba.post.ui.page.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.huanchengfly.tieba.core.ui.navigation.HomeNavigationActions
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.ui.page.destinations.AboutPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.AppThemePageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.HistoryPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.HotTopicListPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.LoginPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.SearchPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.SettingsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.SubPostsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadStorePageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.WebViewPageDestination
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.ramcosta.composedestinations.spec.Direction

@Composable
fun ProvideHomeNavigationActions(
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val appContext = LocalContext.current.applicationContext
    val actions = remember(navController, appContext) {
        object : HomeNavigationActions {
            override fun openSearch() {
                navController.navigateDirection(SearchPageDestination)
            }

            override fun openHistory() {
                navController.navigateDirection(HistoryPageDestination)
            }

            override fun openSettings() {
                navController.navigateDirection(SettingsPageDestination)
            }

            override fun openUserProfile(userId: Long?) {
                val target = userId ?: AccountUtil.currentAccount?.uid?.toLongOrNull()
                if (target != null) {
                    navController.navigateDirection(UserProfilePageDestination(target))
                } else {
                    navController.navigateDirection(LoginPageDestination)
                }
            }

            override fun openAbout() {
                navController.navigateDirection(AboutPageDestination)
            }

            override fun openThreadStore() {
                navController.navigateDirection(ThreadStorePageDestination)
            }

            override fun openAppTheme() {
                navController.navigateDirection(AppThemePageDestination)
            }

            override fun openWeb(url: String) {
                if (url.isBlank()) return
                navController.navigateDirection(WebViewPageDestination(url))
            }

            override fun openForum(forumName: String) {
                navController.navigateDirection(ForumPageDestination(forumName))
            }

            override fun openThread(
                threadId: Long,
                forumId: Long?,
                postId: Long,
                seeLz: Boolean,
                sortType: Int,
                from: String,
                threadInfo: ThreadInfo?,
                scrollToReply: Boolean,
            ) {
                navController.navigateDirection(
                    ThreadPageDestination(
                        threadId = threadId,
                        forumId = forumId,
                        postId = postId,
                        seeLz = seeLz,
                        sortType = sortType,
                        from = from,
                        threadInfo = threadInfo,
                        scrollToReply = scrollToReply
                    )
                )
            }

            override fun openSubPosts(
                threadId: Long,
                subPostId: Long,
                postId: Long,
                loadFromSubPost: Boolean,
            ) {
                navController.navigateDirection(
                    SubPostsPageDestination(
                        threadId = threadId,
                        postId = postId,
                        subPostId = subPostId,
                        loadFromSubPost = loadFromSubPost
                    )
                )
            }

            override fun openHotTopicList() {
                navController.navigateDirection(HotTopicListPageDestination)
            }

            override fun openLogin() {
                navController.navigateDirection(LoginPageDestination)
            }

            override fun copyText(text: String, isSensitive: Boolean) {
                TiebaUtil.copyText(appContext, text, isSensitive = isSensitive)
            }

            override fun startSign() {
                TiebaUtil.startSign(appContext)
            }
        }
    }

    CompositionLocalProvider(
        LocalHomeNavigation provides actions,
        content = content
    )
}

private fun NavHostController.navigateDirection(
    direction: Direction,
    builder: NavOptionsBuilder.() -> Unit = {
        launchSingleTop = true
    }
) {
    navigate(direction.route, builder)
}
