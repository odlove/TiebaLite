package com.huanchengfly.tieba.post.navigation

import android.net.Uri
import androidx.navigation.NavHostController
import com.huanchengfly.tieba.post.runtime.preview.ClipBoardLink
import com.huanchengfly.tieba.post.runtime.preview.ForumLink
import com.huanchengfly.tieba.post.runtime.preview.ThreadLink

class ClipBoardLinkNavigator(
    private val navControllerProvider: () -> NavHostController?,
) {
    fun open(link: ClipBoardLink) {
        val navController = navControllerProvider() ?: return
        when (link) {
            is ThreadLink -> {
                navController.navigate(Uri.parse("tblite://thread/${link.threadId}"))
            }

            is ForumLink -> {
                navController.navigate(Uri.parse("tblite://forum/${link.forumName}"))
            }

            else -> {
                // no-op
            }
        }
    }
}
