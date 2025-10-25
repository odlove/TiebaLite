package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.models.PostMeta

/**
 * Post Proto 与 PostEntity Domain Model 的映射器
 */
object PostMapper {
    /**
     * 从 Post Proto 转换为 PostEntity
     *
     * 提取常用字段到顶层便于访问，同时保留 proto 引用。
     *
     * **字段说明**:
     * - `meta.agreeNum` 使用 Post.agree?.diffAgreeNum（净点赞数，详情页楼层使用）
     * - `meta.hasAgree` 使用 Post.agree?.hasAgree，表示用户是否点赞
     * - `proto` 保留完整引用
     *
     * @param proto Post Proto 对象
     * @param threadId 所属线程ID（Post.tid）
     * @return PostEntity Domain Model
     */
    fun fromProto(proto: Post, threadId: Long): PostEntity {
        return PostEntity(
            id = proto.id,
            threadId = threadId,
            floor = proto.floor,
            time = proto.time,

            author = proto.author,
            authorId = proto.author_id,

            content = proto.content,
            subPostNumber = proto.sub_post_number,

            meta = PostMeta(
                hasAgree = proto.agree?.hasAgree ?: 0,
                agreeNum = (proto.agree?.diffAgreeNum ?: 0L).toInt()  // ✅ 使用 diffAgreeNum（净点赞数）
            ),

            proto = proto
        )
    }

    /**
     * 批量转换 Post 列表
     *
     * @param protos Post Proto 列表
     * @param threadId 所属线程ID
     * @return PostEntity 列表
     */
    fun fromProtos(protos: List<Post>, threadId: Long): List<PostEntity> {
        return protos.map { fromProto(it, threadId) }
    }
}
