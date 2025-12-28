package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.reply.AddPostResult
import com.huanchengfly.tieba.core.network.exception.TiebaLocalException
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse

fun AddPostResponse.toAddPostResult(): AddPostResult {
    val data = data_ ?: throw TiebaLocalException(-1, "未知错误")
    val threadId = data.tid.toLongOrNull() ?: throw TiebaLocalException(-1, "未知错误")
    val postId = data.pid.toLongOrNull() ?: throw TiebaLocalException(-1, "未知错误")
    return AddPostResult(
        threadId = threadId,
        postId = postId,
        expInc = data.exp?.inc.orEmpty(),
    )
}
