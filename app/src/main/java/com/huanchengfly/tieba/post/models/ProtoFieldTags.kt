package com.huanchengfly.tieba.post.models

/**
 * ThreadInfo Proto 字段 Tag 编号定义
 *
 * Protocol Buffer 协议使用 tag 编号来标识每个字段
 * 这个类定义了所有常用 ThreadInfo 字段的 tag 编号
 *
 * 这些常量用于：
 * 1. ThreadInfoDeserializer - 追踪网络数据中实际出现的字段
 * 2. FieldPresence 判断 - 检查某个字段是否在网络数据中出现
 */
object ProtoFieldTags {
    // === 核心标识字段 ===
    const val TAG_ID = 1                    // id: int64
    const val TAG_THREAD_ID = 2             // threadId: int64

    // === 基本信息字段 ===
    const val TAG_TITLE = 3                 // title: string
    const val TAG_REPLY_NUM = 4             // replyNum: int32
    const val TAG_VIEW_NUM = 5              // viewNum: int32
    const val TAG_LAST_TIME = 6             // lastTime: string
    const val TAG_LAST_TIME_INT = 7        // lastTimeInt: int32

    // === 分类标记字段 ===
    const val TAG_THREAD_TYPES = 8          // threadTypes: int32
    const val TAG_IS_TOP = 9                // isTop: int32
    const val TAG_IS_GOOD = 10              // isGood: int32

    // === 用户相关字段 ===
    const val TAG_AUTHOR = 18               // author: User
    const val TAG_LAST_REPLYER = 19         // lastReplyer: User
    const val TAG_COMMENT_NUM = 20          // commentNum: int32

    // === 内容预览字段 ===
    const val TAG_ABSTRACT = 21             // _abstract: Abstract[]（重要！）
    const val TAG_MEDIA = 22                // media: Media[]（重要！列表显示图片）

    // === 论坛信息字段 ===
    const val TAG_FORUM_ID = 27             // forumId: int64
    const val TAG_FORUM_NAME = 28           // forumName: string

    // === 其他基本字段 ===
    const val TAG_IS_NO_TITLE = 38          // isNoTitle: int32
    const val TAG_FIRST_POST_ID = 40        // firstPostId: int64
    const val TAG_CREATE_TIME = 45          // createTime: int32

    // === 收藏相关字段 ===
    const val TAG_COLLECT_STATUS = 50       // collectStatus: int32
    const val TAG_COLLECT_MARK_PID = 51     // collectMarkPid: string
    const val TAG_POST_ID = 52              // post_id: int64
    const val TAG_IS_MEMBER_TOP = 54        // isMemberTop: int32

    // === 作者 ID ===
    const val TAG_AUTHOR_ID = 56            // authorId: int64

    // === 其他信息 ===
    const val TAG_PIDS = 61                 // pids: string
    const val TAG_TWZHIBO_INFO = 72         // twzhibo_info: ZhiBoInfoTW
    const val TAG_VIDEO_INFO = 79           // videoInfo: VideoInfo（重要！）

    // === 富文本内容 ===
    const val TAG_RICH_TITLE = 111          // richTitle: PbContent[]
    const val TAG_RICH_ABSTRACT = 112       // richAbstract: PbContent[]
    const val TAG_ALA_INFO = 113            // ala_info: AlaLiveInfo

    // === 不赞同信息 ===
    const val TAG_DISLIKE_INFO = 120        // dislikeInfo: DislikeInfo[]

    // === 点赞相关 ===
    const val TAG_AGREE_NUM = 124           // agreeNum: int32（重要！）
    const val TAG_AGREE = 126               // agree: Agree（重要！）

    // === 分享相关 ===
    const val TAG_SHARE_NUM = 135           // shareNum: int64

    // === 转发信息 ===
    const val TAG_ORIGIN_THREAD_INFO = 141  // origin_thread_info: OriginThreadInfo
    const val TAG_FIRST_POST_CONTENT = 142  // firstPostContent: PbContent[]
    const val TAG_IS_SHARE_THREAD = 143     // is_share_thread: int32

    // === 话题相关 ===
    const val TAG_IS_TOPIC = 148            // isTopic: int32
    const val TAG_TOPIC_USER_NAME = 149     // topicUserName: string
    const val TAG_TOPIC_H5_URL = 150        // topicH5Url: string

    // === 论坛信息对象 ===
    const val TAG_FORUM_INFO = 155          // forumInfo: SimpleForum

    // === 分享图片 ===
    const val TAG_T_SHARE_IMG = 159         // tShareImg: string

    // === 其他标识 ===
    const val TAG_NID = 164                 // nid: string
    const val TAG_TAB_ID = 175              // tabId: int32
    const val TAG_TAB_NAME = 176            // tabName: string

    // === 删除标记 ===
    const val TAG_IS_DELETED = 181          // isDeleted: int32

    // === 热度 ===
    const val TAG_HOT_NUM = 182             // hotNum: int32

    // === 关键字段判断方法 ===

    /**
     * 检查某个字段是否在网络数据中出现
     *
     * @param presentTags 从 FieldPresence.presentTags 获取
     * @param tag 要检查的 tag 编号
     * @return 字段是否出现
     */
    fun hasTag(presentTags: Set<Int>, tag: Int): Boolean {
        return presentTags.contains(tag)
    }

    /**
     * 检查内容预览字段是否出现
     */
    fun hasAbstract(presentTags: Set<Int>): Boolean = hasTag(presentTags, TAG_ABSTRACT)

    /**
     * 检查媒体字段是否出现
     */
    fun hasMedia(presentTags: Set<Int>): Boolean = hasTag(presentTags, TAG_MEDIA)

    /**
     * 检查视频信息是否出现
     */
    fun hasVideoInfo(presentTags: Set<Int>): Boolean = hasTag(presentTags, TAG_VIDEO_INFO)

    /**
     * 检查点赞数是否出现
     */
    fun hasAgreeNum(presentTags: Set<Int>): Boolean = hasTag(presentTags, TAG_AGREE_NUM)

    /**
     * 检查点赞对象是否出现
     */
    fun hasAgree(presentTags: Set<Int>): Boolean = hasTag(presentTags, TAG_AGREE)
}
