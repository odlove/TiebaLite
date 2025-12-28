package com.huanchengfly.tieba.post.ui.page.thread.mapper

import com.huanchengfly.tieba.core.common.thread.ThreadPost
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.ui.page.thread.PostItemData
import com.huanchengfly.tieba.post.ui.page.thread.SubPostItemData
import com.huanchengfly.tieba.post.ui.page.thread.getContentText
import kotlinx.collections.immutable.toImmutableList

object ThreadPostMapper {
    fun mapPosts(posts: List<ThreadPost>, threadAuthorId: Long? = null): List<PostItemData> =
        posts.map { mapPost(it, threadAuthorId) }

    fun mapPost(post: ThreadPost, threadAuthorId: Long? = null): PostItemData {
        val subPosts = post.subPosts.map {
            SubPostItemData(
                it.wrapImmutable(),
                it.getContentText(threadAuthorId)
            )
        }.toImmutableList()
        return PostItemData(post.wrapImmutable(), subPosts = subPosts)
    }
}
