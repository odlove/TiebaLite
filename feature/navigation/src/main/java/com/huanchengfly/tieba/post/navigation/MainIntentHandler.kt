package com.huanchengfly.tieba.post.navigation

import android.content.Intent
import com.huanchengfly.tieba.post.ui.page.forum.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.thread.destinations.ThreadPageDestination
import com.ramcosta.composedestinations.spec.Direction

class MainIntentHandler(
    private val navigate: (Direction) -> Unit,
) {
    fun handleIntent(intent: Intent): Boolean {
        return if (intent.data?.scheme == "com.baidu.tieba" && intent.data?.host == "unidispatch") {
            val uri = intent.data ?: return false
            when (uri.path.orEmpty().lowercase()) {
                "/frs" -> {
                    val forumName = uri.getQueryParameter("kw") ?: return true
                    navigate(ForumPageDestination(forumName))
                }

                "/pb" -> {
                    val threadId = uri.getQueryParameter("tid")?.toLongOrNull() ?: return true
                    navigate(ThreadPageDestination(threadId))
                }
            }
            true
        } else return if (intent.data?.host == "tieba.baidu.com") {
            val uri = intent.data ?: return false
            when {
                uri.path.orEmpty().lowercase() == "/f" -> {
                    val forumName = uri.getQueryParameter("kw") ?: return true
                    navigate(ForumPageDestination(forumName))
                }

                uri.path.orEmpty().lowercase().startsWith("/p/") -> {
                    val threadId = uri.pathSegments.getOrNull(1)?.toLongOrNull() ?: return true
                    navigate(ThreadPageDestination(threadId))
                }
            }
            true
        } else {
            false
        }
    }
}
