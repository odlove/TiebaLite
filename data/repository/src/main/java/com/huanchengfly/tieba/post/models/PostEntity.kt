package com.huanchengfly.tieba.post.models

import android.os.SystemClock
import com.huanchengfly.tieba.post.api.models.protos.PbContent
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.User

/**
 * Post 实体 - 楼层数据
 *
 * 从 Post Proto 中提取常用字段到顶层，便于高效访问。
 * 同时保留 proto 字段用于访问完整数据。
 */
data class PostEntity(
    val id: Long,
    val threadId: Long,
    val floor: Int,
    val time: Int,
    val author: User?,
    val authorId: Long,
    val content: List<PbContent>,
    val subPostNumber: Int,
    val meta: PostMeta,
    val proto: Post,
    val timestamp: Long = SystemClock.elapsedRealtime()
)

/**
 * Post 可变状态
 */
data class PostMeta(
    val hasAgree: Int = 0,
    val agreeNum: Int = 0
)
