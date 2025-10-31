package com.huanchengfly.tieba.post.models

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
 * @param timestamp 时间戳，用于数据更新时的时间戳管理
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

    // === 字段出现标记 ===
    val presence: FieldPresence = FieldPresence(),  // ✅ 新增：记录网络数据中实际出现的字段

    // === 时间戳 ===
    val timestamp: Long = SystemClock.elapsedRealtime()
) {
    /**
     * 与来自 pbPage API（详情页）的数据合并
     *
     * pbPage API 返回的数据不完整（缺少 abstract、media 等字段），
     * 本方法只更新 pbPage 能提供的字段（meta、replyNum、viewNum 等），
     * 其他字段保持原有值（来自 list API 的完整数据）。
     *
     * @param detail 来自 pbPage API 的新数据
     * @return 合并后的 ThreadEntity
     */
    fun mergeWithDetail(detail: ThreadEntity): ThreadEntity {
        val tags = detail.presence.presentTags
        val old = this.proto

        // ✅ 合并 proto：只覆盖 pbPage 返回的字段，保留 list API 的完整数据
        // 基础使用旧 proto，这样未显式列出的字段会自动保持原值
        val mergedProto = this.proto.copy(
            // === 基本信息字段 ===
            replyNum = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_REPLY_NUM))
                detail.proto.replyNum else old.replyNum,
            viewNum = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_VIEW_NUM))
                detail.proto.viewNum else old.viewNum,
            title = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_TITLE))
                detail.proto.title else old.title,
            lastTime = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_LAST_TIME))
                detail.proto.lastTime else old.lastTime,
            lastTimeInt = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_LAST_TIME_INT))
                detail.proto.lastTimeInt else old.lastTimeInt,

            // === 分类标记字段 ===
            threadTypes = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_THREAD_TYPES))
                detail.proto.threadTypes else old.threadTypes,
            isTop = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_IS_TOP))
                detail.proto.isTop else old.isTop,
            isGood = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_IS_GOOD))
                detail.proto.isGood else old.isGood,
            isDeleted = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_IS_DELETED))
                detail.proto.isDeleted else old.isDeleted,

            // === 用户相关字段 ===
            author = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_AUTHOR))
                detail.proto.author else old.author,
            authorId = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_AUTHOR_ID))
                detail.proto.authorId else old.authorId,
            commentNum = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_COMMENT_NUM))
                detail.proto.commentNum else old.commentNum,
            lastReplyer = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_LAST_REPLYER))
                detail.proto.lastReplyer else old.lastReplyer,

            // === 论坛信息字段 ===
            forumId = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_FORUM_ID))
                detail.proto.forumId else old.forumId,
            forumName = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_FORUM_NAME))
                detail.proto.forumName else old.forumName,
            forumInfo = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_FORUM_INFO))
                detail.proto.forumInfo else old.forumInfo,

            // === 其他基本字段 ===
            firstPostId = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_FIRST_POST_ID))
                detail.proto.firstPostId else old.firstPostId,
            createTime = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_CREATE_TIME))
                detail.proto.createTime else old.createTime,
            isNoTitle = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_IS_NO_TITLE))
                detail.proto.isNoTitle else old.isNoTitle,

            // === 收藏相关字段 ===
            collectStatus = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_COLLECT_STATUS))
                detail.proto.collectStatus else old.collectStatus,
            collectMarkPid = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_COLLECT_MARK_PID))
                detail.proto.collectMarkPid else old.collectMarkPid,
            post_id = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_POST_ID))
                detail.proto.post_id else old.post_id,
            isMemberTop = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_IS_MEMBER_TOP))
                detail.proto.isMemberTop else old.isMemberTop,

            // === 点赞相关 ===
            agree = if (ProtoFieldTags.hasAgree(tags))
                detail.proto.agree else old.agree,
            agreeNum = if (ProtoFieldTags.hasAgreeNum(tags))
                detail.proto.agreeNum else old.agreeNum,

            // === 统计字段 ===
            shareNum = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_SHARE_NUM))
                detail.proto.shareNum else old.shareNum,
            hotNum = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_HOT_NUM))
                detail.proto.hotNum else old.hotNum,

            // === 其他字段 ===
            pids = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_PIDS))
                detail.proto.pids else old.pids,
            twzhibo_info = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_TWZHIBO_INFO))
                detail.proto.twzhibo_info else old.twzhibo_info,
            videoInfo = if (ProtoFieldTags.hasVideoInfo(tags))
                detail.proto.videoInfo else old.videoInfo,

            // === 富文本内容 ===
            richTitle = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_RICH_TITLE))
                detail.proto.richTitle else old.richTitle,
            richAbstract = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_RICH_ABSTRACT))
                detail.proto.richAbstract else old.richAbstract,
            ala_info = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_ALA_INFO))
                detail.proto.ala_info else old.ala_info,

            // === 不赞同信息 ===
            dislikeInfo = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_DISLIKE_INFO))
                detail.proto.dislikeInfo else old.dislikeInfo,

            // === 转发信息 ===
            origin_thread_info = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_ORIGIN_THREAD_INFO))
                detail.proto.origin_thread_info else old.origin_thread_info,
            firstPostContent = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_FIRST_POST_CONTENT))
                detail.proto.firstPostContent else old.firstPostContent,
            is_share_thread = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_IS_SHARE_THREAD))
                detail.proto.is_share_thread else old.is_share_thread,

            // === 话题相关 ===
            isTopic = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_IS_TOPIC))
                detail.proto.isTopic else old.isTopic,
            topicUserName = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_TOPIC_USER_NAME))
                detail.proto.topicUserName else old.topicUserName,
            topicH5Url = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_TOPIC_H5_URL))
                detail.proto.topicH5Url else old.topicH5Url,

            // === 分享图片 ===
            tShareImg = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_T_SHARE_IMG))
                detail.proto.tShareImg else old.tShareImg,

            // === 其他标识 ===
            nid = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_NID))
                detail.proto.nid else old.nid,
            tabId = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_TAB_ID))
                detail.proto.tabId else old.tabId,
            tabName = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_TAB_NAME))
                detail.proto.tabName else old.tabName
        )

        return this.copy(
            // === 更新顶层字段 ===
            replyNum = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_REPLY_NUM))
                detail.replyNum else this.replyNum,
            viewNum = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_VIEW_NUM))
                detail.viewNum else this.viewNum,

            // === 合并 meta（通过专用方法）===
            meta = this.meta.merge(detail.meta, tags),

            // === 更新 proto（已合并）===
            proto = mergedProto,

            // === ✅ 更新 presence：union 而不是替换 ===
            presence = FieldPresence(presentTags = this.presence.presentTags + detail.presence.presentTags),

            // === 其他字段保持不变 ===
            // abstract、media 等来自 list API，不覆盖
        )
    }
}

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
) {
    /**
     * 与来自 pbPage API 的 meta 合并
     *
     * 根据 presence 信息判断新数据中哪些字段实际出现，
     * 只有确实出现的字段才覆盖旧值，其他字段保持不变。
     *
     * @param detail 来自 pbPage API 的新 meta
     * @param tags 新数据中出现的 proto tag 集合
     * @return 合并后的 meta
     */
    fun merge(detail: ThreadMeta, tags: Set<Int>): ThreadMeta {
        return this.copy(
            // hasAgree：只有 agree 对象出现才更新
            hasAgree = if (ProtoFieldTags.hasAgree(tags))
                detail.hasAgree
            else
                this.hasAgree,

            // agreeNum：只有该字段出现才更新
            agreeNum = if (ProtoFieldTags.hasAgreeNum(tags) && detail.agreeNum >= 0)
                detail.agreeNum
            else
                this.agreeNum,

            // ✅ collectStatus：只有该字段在网络数据中出现才更新
            collectStatus = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_COLLECT_STATUS) && detail.collectStatus >= 0)
                detail.collectStatus
            else
                this.collectStatus,

            // ✅ collectMarkPid：只有该字段在网络数据中出现才更新
            collectMarkPid = if (ProtoFieldTags.hasTag(tags, ProtoFieldTags.TAG_COLLECT_MARK_PID))
                detail.collectMarkPid
            else
                this.collectMarkPid
        )
    }
}

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
 * @param timestamp 时间戳
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
