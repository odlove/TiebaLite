package com.huanchengfly.tieba.post.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.huanchengfly.tieba.core.common.thread.ThreadPreview
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.ui.navigation.HomeNavigationActions
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.common.reply.ReplyArgs
import com.huanchengfly.tieba.post.activities.AppFontSizeActivity
import com.huanchengfly.tieba.post.activities.TranslucentThemeActivity
import com.huanchengfly.tieba.post.ui.page.forum.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.history.destinations.HistoryPageDestination
import com.huanchengfly.tieba.post.ui.page.hottopic.list.destinations.HotTopicListPageDestination
import com.huanchengfly.tieba.post.ui.page.login.destinations.LoginPageDestination
import com.huanchengfly.tieba.post.ui.page.search.destinations.SearchPageDestination
import com.huanchengfly.tieba.post.ui.page.reply.destinations.ReplyPageDestination
import com.huanchengfly.tieba.post.ui.page.subposts.destinations.SubPostsPageDestination
import com.huanchengfly.tieba.post.ui.page.subposts.destinations.SubPostsSheetPageDestination
import com.huanchengfly.tieba.post.ui.page.thread.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.threadcollect.destinations.ThreadCollectPageDestination
import com.huanchengfly.tieba.post.ui.page.user.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.settings.destinations.AboutPageDestination
import com.huanchengfly.tieba.post.ui.page.settings.destinations.AppThemePageDestination
import com.huanchengfly.tieba.post.ui.page.settings.destinations.SettingsPageDestination
import com.huanchengfly.tieba.post.ui.page.webview.destinations.WebViewPageDestination
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.sign.SignActions
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.ramcosta.composedestinations.spec.Direction

@Composable
fun ProvideHomeNavigationActions(
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val actions = remember(navController, appContext, context) {
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

            override fun openThreadCollect() {
                navController.navigateDirection(ThreadCollectPageDestination)
            }

            override fun openAppTheme() {
                navController.navigateDirection(AppThemePageDestination)
            }

            override fun openFontSizeSettings() {
                context.startActivity(Intent(context, AppFontSizeActivity::class.java))
            }

            override fun openTranslucentTheme(channel: ThemeChannel) {
                context.startActivity(
                    Intent(context, TranslucentThemeActivity::class.java)
                        .putExtra(TranslucentThemeActivity.EXTRA_THEME_CHANNEL, channel.name)
                )
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
                threadPreview: ThreadPreview?,
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
                        threadPreview = threadPreview,
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

            override fun openSubPostsSheet(
                threadId: Long,
                subPostId: Long,
                postId: Long,
                loadFromSubPost: Boolean,
            ) {
                navController.navigateDirection(
                    SubPostsSheetPageDestination(
                        threadId = threadId,
                        postId = postId,
                        subPostId = subPostId,
                        loadFromSubPost = loadFromSubPost
                    )
                )
            }

            override fun openReply(args: ReplyArgs) {
                navController.navigateDirection(
                    ReplyPageDestination(
                        forumId = args.forumId,
                        forumName = args.forumName,
                        threadId = args.threadId,
                        postId = args.postId,
                        subPostId = args.subPostId,
                        replyUserId = args.replyUserId,
                        replyUserName = args.replyUserName,
                        replyUserPortrait = args.replyUserPortrait,
                        tbs = args.tbs,
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

            override fun shareText(text: String, title: String?) {
                TiebaUtil.shareText(appContext, text, title)
            }

            override fun startSign() {
                SignActions.startSign(appContext)
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
