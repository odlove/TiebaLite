package com.huanchengfly.tieba.post.repository.utils

import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo

/**
 * Proto 字段智能合并工具
 *
 * 解决 API 返回数据不一致的问题：
 * - 列表页返回的 ThreadInfo：有 title, agreeNum, media 等
 * - 详情页返回的 ThreadInfo：可能缺少 createTime, _abstract, media 等
 *
 * 简单替换会导致：
 *   ❌ 时间显示为 0
 *   ❌ 图片丢失
 *   ❌ 摘要丢失
 *
 * 解决方案：字段级智能合并 - 有值的字段用新数据，无值的字段沿用旧数据
 */
object ProtoMergeUtils {

    /**
     * 智能合并 ThreadInfo Proto 字段
     *
     * **问题**: 详情页返回的 ThreadInfo 经常缺少列表页所需字段（createTime, media, _abstract 等），
     * 直接替换会导致时间显示为 0、图片丢失。
     *
     * **解决方案**: 字段级智能合并 - 有值的字段用新数据，无值的字段沿用旧数据。
     *
     * @param oldProto 旧 ThreadInfo（可能为 null）
     * @param newProto 新 ThreadInfo
     * @return 合并后的 ThreadInfo
     */
    fun mergeThreadProto(oldProto: ThreadInfo?, newProto: ThreadInfo): ThreadInfo {
        if (oldProto == null) return newProto

        return newProto.copy(
            // === 核心标识 ===
            threadId = newProto.id,  // 直接使用 id（与 ThreadMapper 一致）
            id = newProto.id,
            firstPostId = newProto.firstPostId,

            // === 基本信息（优先使用新数据，防止空/0 值） ===
            title = newProto.title.ifBlank { oldProto.title },
            replyNum = newProto.replyNum.takeIf { it != 0 } ?: oldProto.replyNum,
            viewNum = newProto.viewNum.takeIf { it != 0 } ?: oldProto.viewNum,

            // === 时间字段（✅ 关键修复：防止时间重置为 0） ===
            createTime = newProto.createTime.takeIf { it != 0 } ?: oldProto.createTime,
            lastTimeInt = newProto.lastTimeInt.takeIf { it != 0 } ?: oldProto.lastTimeInt,

            // === 论坛信息（优先使用新数据） ===
            forumId = newProto.forumId.takeIf { it != 0L } ?: oldProto.forumId,
            forumName = newProto.forumName.ifBlank { oldProto.forumName },

            // === 作者信息（优先使用新数据） ===
            author = newProto.author ?: oldProto.author,
            authorId = newProto.authorId.takeIf { it != 0L } ?: oldProto.authorId,

            // === 内容预览（✅ 关键修复：防止摘要和图片丢失） ===
            _abstract = newProto._abstract.takeIf { it.isNotEmpty() } ?: oldProto._abstract,
            media = newProto.media.takeIf { it.isNotEmpty() } ?: oldProto.media,

            // === 视频信息（✅ 关键修复：防止视频信息丢失） ===
            videoInfo = newProto.videoInfo ?: oldProto.videoInfo,

            // === 分类标记（优先使用新数据，默认为 0） ===
            isTop = if (newProto.isTop != 0) newProto.isTop else oldProto.isTop,
            isGood = if (newProto.isGood != 0) newProto.isGood else oldProto.isGood,
            isDeleted = if (newProto.isDeleted != 0) newProto.isDeleted else oldProto.isDeleted,

            // === 论坛详细信息（✅ 关键修复：防止推荐页论坛头像丢失） ===
            forumInfo = newProto.forumInfo ?: oldProto.forumInfo,

            // === 转发帖信息（✅ 关键修复：防止转发帖原帖信息丢失） ===
            origin_thread_info = newProto.origin_thread_info ?: oldProto.origin_thread_info,

            // === 首楼内容（✅ 关键修复：防止首楼内容丢失） ===
            firstPostContent = newProto.firstPostContent.takeIf { it.isNotEmpty() } ?: oldProto.firstPostContent,

            // === 富文本内容（✅ 关键修复：防止富文本标题和摘要丢失） ===
            richTitle = newProto.richTitle.takeIf { it.isNotEmpty() } ?: oldProto.richTitle,
            richAbstract = newProto.richAbstract.takeIf { it.isNotEmpty() } ?: oldProto.richAbstract,

            // === 最后回复信息（✅ 关键修复：防止最后回复者信息丢失） ===
            lastReplyer = newProto.lastReplyer ?: oldProto.lastReplyer,
            lastTime = newProto.lastTime.ifBlank { oldProto.lastTime }
        )
    }
}
