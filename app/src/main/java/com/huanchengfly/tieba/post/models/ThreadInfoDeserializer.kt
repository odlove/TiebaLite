package com.huanchengfly.tieba.post.models

import android.util.Log
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoReader
import okio.ByteString

/**
 * ThreadInfo 反序列化器 - 追踪网络数据中实际出现的字段
 *
 * 在解析 ThreadInfo proto 时，记录哪些字段在网络数据中真实出现过
 * 这样就能精确区分"API 没返回"和"API 返回了默认值"
 */
object ThreadInfoDeserializer {

    /**
     * 带字段追踪的反序列化
     *
     * @param reader ProtoReader 对象
     * @return ThreadInfoWithPresence - 包含 proto 和字段出现标记
     */
    fun decodeWithPresence(reader: ProtoReader): ThreadInfoWithPresence {
        // 追踪出现过的 tag
        val presentTags = mutableSetOf<Int>()

        // 复制 Wire 生成的 decode 逻辑，但记录每个 tag
        var id: Long = 0L
        var threadId: Long = 0L
        var title: String = ""
        var replyNum: Int = 0
        var viewNum: Int = 0
        var lastTime: String = ""
        var lastTimeInt: Int = 0
        var threadTypes: Int = 0
        var isTop: Int = 0
        var isGood: Int = 0
        var author: com.huanchengfly.tieba.post.api.models.protos.User? = null
        var lastReplyer: com.huanchengfly.tieba.post.api.models.protos.User? = null
        var commentNum: Int = 0
        val _abstract = mutableListOf<com.huanchengfly.tieba.post.api.models.protos.Abstract>()
        val media = mutableListOf<com.huanchengfly.tieba.post.api.models.protos.Media>()
        var forumId: Long = 0L
        var forumName: String = ""
        var isNoTitle: Int = 0
        var firstPostId: Long = 0L
        var createTime: Int = 0
        var collectStatus: Int = 0
        var collectMarkPid: String = ""
        var post_id: Long = 0L
        var isMemberTop: Int = 0
        var authorId: Long = 0L
        var pids: String = ""
        var twzhibo_info: com.huanchengfly.tieba.post.api.models.protos.ZhiBoInfoTW? = null
        var videoInfo: com.huanchengfly.tieba.post.api.models.protos.VideoInfo? = null
        val richTitle = mutableListOf<com.huanchengfly.tieba.post.api.models.protos.PbContent>()
        val richAbstract = mutableListOf<com.huanchengfly.tieba.post.api.models.protos.PbContent>()
        var ala_info: com.huanchengfly.tieba.post.api.models.protos.AlaLiveInfo? = null
        val dislikeInfo = mutableListOf<com.huanchengfly.tieba.post.api.models.protos.DislikeInfo>()
        var agreeNum: Int = 0
        var agree: com.huanchengfly.tieba.post.api.models.protos.Agree? = null
        var shareNum: Long = 0L
        var origin_thread_info: com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo? = null
        val firstPostContent = mutableListOf<com.huanchengfly.tieba.post.api.models.protos.PbContent>()
        var is_share_thread: Int = 0
        var isTopic: Int = 0
        var topicUserName: String = ""
        var topicH5Url: String = ""
        var forumInfo: com.huanchengfly.tieba.post.api.models.protos.SimpleForum? = null
        var tShareImg: String = ""
        var nid: String = ""
        var tabId: Int = 0
        var tabName: String = ""
        var isDeleted: Int = 0
        var hotNum: Int = 0

        val unknownFields = reader.forEachTag { tag ->
            presentTags.add(tag)  // ✅ 记录这个 tag 出现过了

            when (tag) {
                1 -> id = ProtoAdapter.INT64.decode(reader)
                2 -> threadId = ProtoAdapter.INT64.decode(reader)
                3 -> title = ProtoAdapter.STRING.decode(reader)
                4 -> replyNum = ProtoAdapter.INT32.decode(reader)
                5 -> viewNum = ProtoAdapter.INT32.decode(reader)
                6 -> lastTime = ProtoAdapter.STRING.decode(reader)
                7 -> lastTimeInt = ProtoAdapter.INT32.decode(reader)
                8 -> threadTypes = ProtoAdapter.INT32.decode(reader)
                9 -> isTop = ProtoAdapter.INT32.decode(reader)
                10 -> isGood = ProtoAdapter.INT32.decode(reader)
                18 -> author = com.huanchengfly.tieba.post.api.models.protos.User.ADAPTER.decode(reader)
                19 -> lastReplyer = com.huanchengfly.tieba.post.api.models.protos.User.ADAPTER.decode(reader)
                20 -> commentNum = ProtoAdapter.INT32.decode(reader)
                21 -> _abstract.add(com.huanchengfly.tieba.post.api.models.protos.Abstract.ADAPTER.decode(reader))
                22 -> media.add(com.huanchengfly.tieba.post.api.models.protos.Media.ADAPTER.decode(reader))
                27 -> forumId = ProtoAdapter.INT64.decode(reader)
                28 -> forumName = ProtoAdapter.STRING.decode(reader)
                38 -> isNoTitle = ProtoAdapter.INT32.decode(reader)
                40 -> firstPostId = ProtoAdapter.INT64.decode(reader)
                45 -> createTime = ProtoAdapter.INT32.decode(reader)
                50 -> collectStatus = ProtoAdapter.INT32.decode(reader)
                51 -> collectMarkPid = ProtoAdapter.STRING.decode(reader)
                52 -> post_id = ProtoAdapter.INT64.decode(reader)
                54 -> isMemberTop = ProtoAdapter.INT32.decode(reader)
                56 -> authorId = ProtoAdapter.INT64.decode(reader)
                61 -> pids = ProtoAdapter.STRING.decode(reader)
                72 -> twzhibo_info = com.huanchengfly.tieba.post.api.models.protos.ZhiBoInfoTW.ADAPTER.decode(reader)
                79 -> videoInfo = com.huanchengfly.tieba.post.api.models.protos.VideoInfo.ADAPTER.decode(reader)
                111 -> richTitle.add(com.huanchengfly.tieba.post.api.models.protos.PbContent.ADAPTER.decode(reader))
                112 -> richAbstract.add(com.huanchengfly.tieba.post.api.models.protos.PbContent.ADAPTER.decode(reader))
                113 -> ala_info = com.huanchengfly.tieba.post.api.models.protos.AlaLiveInfo.ADAPTER.decode(reader)
                120 -> dislikeInfo.add(com.huanchengfly.tieba.post.api.models.protos.DislikeInfo.ADAPTER.decode(reader))
                124 -> agreeNum = ProtoAdapter.INT32.decode(reader)
                126 -> agree = com.huanchengfly.tieba.post.api.models.protos.Agree.ADAPTER.decode(reader)
                135 -> shareNum = ProtoAdapter.INT64.decode(reader)
                141 -> origin_thread_info = com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo.ADAPTER.decode(reader)
                142 -> firstPostContent.add(com.huanchengfly.tieba.post.api.models.protos.PbContent.ADAPTER.decode(reader))
                143 -> is_share_thread = ProtoAdapter.INT32.decode(reader)
                148 -> isTopic = ProtoAdapter.INT32.decode(reader)
                149 -> topicUserName = ProtoAdapter.STRING.decode(reader)
                150 -> topicH5Url = ProtoAdapter.STRING.decode(reader)
                155 -> forumInfo = com.huanchengfly.tieba.post.api.models.protos.SimpleForum.ADAPTER.decode(reader)
                159 -> tShareImg = ProtoAdapter.STRING.decode(reader)
                164 -> nid = ProtoAdapter.STRING.decode(reader)
                175 -> tabId = ProtoAdapter.INT32.decode(reader)
                176 -> tabName = ProtoAdapter.STRING.decode(reader)
                181 -> isDeleted = ProtoAdapter.INT32.decode(reader)
                182 -> hotNum = ProtoAdapter.INT32.decode(reader)
                else -> reader.readUnknownField(tag)
            }
        }

        val proto = ThreadInfo(
            id, threadId, title, replyNum, viewNum, lastTime, lastTimeInt, threadTypes, isTop,
            isGood, author, lastReplyer, commentNum, _abstract, media, forumId, forumName,
            isNoTitle, firstPostId, createTime, collectStatus, collectMarkPid, post_id,
            isMemberTop, authorId, pids, twzhibo_info, videoInfo, richTitle, richAbstract,
            ala_info, dislikeInfo, agreeNum, agree, shareNum, origin_thread_info,
            firstPostContent, is_share_thread, isTopic, topicUserName, topicH5Url, forumInfo,
            tShareImg, nid, tabId, tabName, isDeleted, hotNum, unknownFields
        )

        // ✅ 直接返回出现过的所有 tag（自动支持所有字段）
        val presence = FieldPresence(presentTags = presentTags)

        return ThreadInfoWithPresence(proto, presence)
    }
}
