package com.huanchengfly.tieba.core.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import com.huanchengfly.tieba.core.common.reply.ReplyArgs
import com.huanchengfly.tieba.core.common.thread.ThreadPreview
import com.huanchengfly.tieba.core.common.theme.ThemeChannel

/**
 * 定义主页面子模块可调用的导航 / 辅助操作，避免直接依赖 :app。
 */
interface HomeNavigationActions {
    fun openSearch()
    fun openHistory()
    fun openSettings()
    fun openUserProfile(userId: Long? = null)
    fun openAbout()
    fun openThreadCollect()
    fun openAppTheme()
    fun openFontSizeSettings()
    fun openTranslucentTheme(channel: ThemeChannel)
    fun openWeb(url: String)
    fun openForum(forumName: String)
    fun openThread(
        threadId: Long,
        forumId: Long? = null,
        postId: Long = 0,
        seeLz: Boolean = false,
        sortType: Int = 0,
        from: String = "",
        threadPreview: ThreadPreview? = null,
        scrollToReply: Boolean = false
    )
    fun openSubPosts(
        threadId: Long,
        subPostId: Long,
        postId: Long = 0,
        loadFromSubPost: Boolean = false
    )
    fun openSubPostsSheet(
        threadId: Long,
        subPostId: Long,
        postId: Long = 0,
        loadFromSubPost: Boolean = false
    )
    fun openReply(args: ReplyArgs)
    fun openHotTopicList()
    fun openLogin()
    fun copyText(text: String, isSensitive: Boolean = false)
    fun shareText(text: String, title: String? = null)
    fun startSign()
}

/**
 * 提供 HomeNavigationActions 的 CompositionLocal。
 */
val LocalHomeNavigation = staticCompositionLocalOf<HomeNavigationActions> {
    error("HomeNavigationActions 未提供")
}
