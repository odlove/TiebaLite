package com.huanchengfly.tieba.post.store.mappers

import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.store.models.ThreadEntity
import com.huanchengfly.tieba.post.store.models.ThreadMeta

/**
 * ThreadInfo Proto 与 ThreadEntity Domain Model 的映射器
 */
object ThreadMapper {
    /**
     * 从 ThreadInfo Proto 转换为 ThreadEntity
     *
     * 提取常用字段到顶层便于访问，同时保留 proto 引用。
     *
     * **字段说明**:
     * - `meta.agreeNum` 使用 ThreadInfo.agreeNum（顶层字段），这是列表页的标准字段
     * - `meta.hasAgree` 使用 ThreadInfo.agree?.hasAgree，表示用户是否点赞
     * - `meta.collectStatus` 和 `collectMarkPid` 从顶层字段提取
     * - `proto` 保留完整引用，用于访问 origin_thread_info 等少用字段
     *
     * @param proto ThreadInfo Proto 对象
     * @return ThreadEntity Domain Model
     */
    fun fromProto(proto: ThreadInfo): ThreadEntity {
        return ThreadEntity(
            // === 核心标识 ===
            threadId = proto.id,  // 直接使用 id（基于 aiotieba 实践）
            firstPostId = proto.firstPostId,

            // === 基本信息 ===
            title = proto.title,
            replyNum = proto.replyNum,
            viewNum = proto.viewNum,
            createTime = proto.createTime,
            lastTimeInt = proto.lastTimeInt,

            // === 分类标记 ===
            isTop = proto.isTop,
            isGood = proto.isGood,
            isDeleted = proto.isDeleted,

            // === 作者信息 ===
            author = proto.author,
            authorId = proto.authorId,

            // === 论坛信息 ===
            forumId = proto.forumId,
            forumName = proto.forumName,

            // === 内容预览 ===
            abstract = proto._abstract,
            media = proto.media,

            // === 视频信息 ===
            videoInfo = proto.videoInfo,

            // === 可变状态 ===
            meta = ThreadMeta(
                hasAgree = proto.agree?.hasAgree ?: 0,
                agreeNum = proto.agreeNum,
                collectStatus = proto.collectStatus,
                collectMarkPid = proto.collectMarkPid.toLongOrNull() ?: 0L
            ),

            // === Proto 引用 ===
            proto = proto
        )
    }

    /**
     * 批量转换 ThreadInfo 列表
     *
     * @param protos ThreadInfo Proto 列表
     * @return ThreadEntity 列表
     */
    fun fromProtos(protos: List<ThreadInfo>): List<ThreadEntity> {
        return protos.map { fromProto(it) }
    }
}
