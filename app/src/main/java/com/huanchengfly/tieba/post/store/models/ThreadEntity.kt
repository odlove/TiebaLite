package com.huanchengfly.tieba.post.store.models

import android.os.SystemClock
import com.huanchengfly.tieba.post.api.models.protos.Abstract
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.PbContent
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.VideoInfo

/**
 * Thread 实体 - Domain Model with Proto reference
 *
 * 从 ThreadInfo Proto 中提取常用字段到顶层，便于高效访问。
 * 同时保留 proto 字段用于访问完整数据（如 origin_thread_info 等）。
 * Meta 和 Entity 完全分离，避免状态不一致和性能问题。
 *
 * @param threadId ThreadInfo.id，线程ID（全局唯一标识符）
 * @param firstPostId ThreadInfo.firstPostId，首楼ID
 * @param title 标题
 * @param replyNum 回复数
 * @param viewNum 浏览数
 * @param createTime 创建时间（Unix 时间戳）
 * @param lastTimeInt 最后回复时间（Unix 时间戳）
 * @param isTop 是否置顶 (0/1)
 * @param isGood 是否精华 (0/1)
 * @param isDeleted 是否已删除 (0/1)
 * @param author 作者信息（保留 Proto User，因为是嵌套小对象）
 * @param authorId 作者ID
 * @param forumId 所属贴吧ID
 * @param forumName 所属贴吧名称
 * @param abstract 内容摘要（用于列表页预览）
 * @param media 媒体列表（图片/视频）
 * @param videoInfo 视频信息（视频帖）
 * @param meta 可变状态（点赞、收藏等高频更新字段）
 * @param proto 完整 Proto 引用（用于访问 origin_thread_info 等少用字段）
 * @param timestamp Store 内部时间戳，用于 TTL 和 LRU 管理
 */
data class ThreadEntity(
    // === 核心标识 ===
    val threadId: Long,
    val firstPostId: Long,

    // === 基本信息 ===
    val title: String,
    val replyNum: Int,
    val viewNum: Int,
    val createTime: Int,
    val lastTimeInt: Int,

    // === 分类标记 ===
    val isTop: Int,
    val isGood: Int,
    val isDeleted: Int,

    // === 作者信息 ===
    val author: User?,
    val authorId: Long,

    // === 论坛信息 ===
    val forumId: Long,
    val forumName: String,

    // === 内容预览 ===
    val abstract: List<Abstract>,
    val media: List<Media>,

    // === 视频信息 ===
    val videoInfo: VideoInfo?,

    // === 可变状态（独立管理）===
    val meta: ThreadMeta,

    // === Proto 引用（只读）===
    val proto: ThreadInfo,

    // === 时间戳 ===
    val timestamp: Long = SystemClock.elapsedRealtime()
)

/**
 * Thread 可变状态
 *
 * 包含高频更新的字段（点赞、收藏），与 ThreadEntity 分离，
 * 避免每次更新都复制整个 Entity。
 *
 * @param hasAgree 用户是否点赞 (0=未点赞, 1=已点赞)
 * @param agreeNum 总点赞数
 * @param collectStatus 收藏状态 (0=未收藏, 1=已收藏)
 * @param collectMarkPid 收藏标记 PID
 */
data class ThreadMeta(
    val hasAgree: Int = 0,
    val agreeNum: Int = 0,
    val collectStatus: Int = 0,
    val collectMarkPid: Long = 0
)

/**
 * Post 实体 - 楼层数据
 *
 * 从 Post Proto 中提取常用字段到顶层，便于高效访问。
 * 同时保留 proto 字段用于访问完整数据。
 *
 * @param id Post.id，楼层ID
 * @param threadId Post.tid，所属线程ID
 * @param floor Post.floor，楼层号
 * @param time Post.time，发布时间（Unix 时间戳）
 * @param author Post.author，作者信息
 * @param authorId Post.author_id，作者ID
 * @param content Post.content，内容列表
 * @param subPostNumber Post.sub_post_number，子楼层数量
 * @param meta 可变状态（点赞等）
 * @param proto 完整 Proto 引用
 * @param timestamp Store 内部时间戳
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
 *
 * @param hasAgree 用户是否点赞 (0/1)
 * @param agreeNum 总点赞数（使用 agree.diffAgreeNum 或 agree.agreeNum）
 */
data class PostMeta(
    val hasAgree: Int = 0,
    val agreeNum: Int = 0
)
