package com.huanchengfly.tieba.core.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo

/**
 * 定义主页面子模块可调用的导航 / 辅助操作，避免直接依赖 :app。
 */
interface HomeNavigationActions {
    fun openSearch()
    fun openHistory()
    fun openSettings()
    fun openUserProfile(userId: Long? = null)
    fun openAbout()
    fun openThreadStore()
    fun openAppTheme()
    fun openWeb(url: String)
    fun openForum(forumName: String)
    fun openThread(
        threadId: Long,
        forumId: Long? = null,
        postId: Long = 0,
        seeLz: Boolean = false,
        sortType: Int = 0,
        from: String = "",
        threadInfo: ThreadInfo? = null,
        scrollToReply: Boolean = false
    )
    fun openSubPosts(
        threadId: Long,
        subPostId: Long,
        postId: Long = 0,
        loadFromSubPost: Boolean = false
    )
    fun openHotTopicList()
    fun openLogin()
    fun copyText(text: String, isSensitive: Boolean = false)
    fun startSign()
}

/**
 * 提供 HomeNavigationActions 的 CompositionLocal。
 */
val LocalHomeNavigation = staticCompositionLocalOf<HomeNavigationActions> {
    error("HomeNavigationActions 未提供")
}
