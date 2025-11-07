package com.huanchengfly.tieba.post.ui.page.thread.mapper

import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.ui.page.thread.PostItemData

object ThreadPostMapper {
    fun mapPosts(posts: List<Post>): List<PostItemData> = posts.map(::mapPost)

    fun mapPost(post: Post): PostItemData = PostItemData(post.wrapImmutable())
}
