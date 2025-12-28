package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.feed.RichTextSegment
import com.huanchengfly.tieba.core.common.feed.ThreadAuthor
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.feed.ThreadForumInfo
import com.huanchengfly.tieba.core.common.feed.ThreadMediaItem
import com.huanchengfly.tieba.core.common.feed.ThreadVideoInfo
import com.huanchengfly.tieba.core.common.forum.ForumClassify
import com.huanchengfly.tieba.core.common.forum.ForumDetailInfo
import com.huanchengfly.tieba.core.common.forum.ForumInfo
import com.huanchengfly.tieba.core.common.forum.ForumPageData
import com.huanchengfly.tieba.core.common.forum.ForumPageInfo
import com.huanchengfly.tieba.core.common.forum.ForumRuleAuthor
import com.huanchengfly.tieba.core.common.forum.ForumRuleDetail
import com.huanchengfly.tieba.core.common.forum.ForumRuleItem
import com.huanchengfly.tieba.core.common.forum.ForumRuleSummary
import com.huanchengfly.tieba.core.common.forum.ForumSignInfo
import com.huanchengfly.tieba.core.common.forum.ForumSignUserInfo
import com.huanchengfly.tieba.post.api.models.protos.BawuRoleInfoPub
import com.huanchengfly.tieba.post.api.models.protos.ForumRule
import com.huanchengfly.tieba.post.api.models.protos.ForumRuleStatus
import com.huanchengfly.tieba.post.api.models.protos.RecommendForumInfo
import com.huanchengfly.tieba.post.api.models.protos.frsPage.Classify
import com.huanchengfly.tieba.post.api.models.protos.frsPage.ForumInfo as FrsForumInfo
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.frsPage.SignInfo
import com.huanchengfly.tieba.post.api.models.protos.frsPage.SignUser
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.forumRuleDetail.ForumRuleDetailResponse

fun SignUser.toForumSignUserInfo(): ForumSignUserInfo =
    ForumSignUserInfo(
        userId = user_id,
        isSignIn = is_sign_in,
        userSignRank = user_sign_rank,
        contSignNum = cont_sign_num,
    )

fun SignInfo.toForumSignInfo(): ForumSignInfo =
    ForumSignInfo(
        userInfo = user_info?.toForumSignUserInfo(),
    )

fun Classify.toForumClassify(): ForumClassify =
    ForumClassify(
        classId = class_id,
        className = class_name,
    )

fun FrsForumInfo.toForumInfo(): ForumInfo =
    ForumInfo(
        id = id,
        name = name,
        avatar = avatar,
        isLike = is_like,
        userLevel = user_level,
        levelName = level_name,
        curScore = cur_score,
        levelupScore = levelup_score,
        signInfo = sign_in_info?.toForumSignInfo(),
        memberNum = member_num,
        threadNum = thread_num,
        postNum = post_num,
        goodClassify = good_classify.map { it.toForumClassify() },
        slogan = slogan,
    )

fun ForumRuleStatus.toForumRuleSummary(): ForumRuleSummary =
    ForumRuleSummary(
        title = title,
        hasForumRule = has_forum_rule == 1,
    )

fun ThreadInfo.toThreadCard(): ThreadCard {
    val author = author?.let {
        ThreadAuthor(
            id = it.id,
            name = it.name,
            nameShow = it.nameShow,
            portrait = it.portrait,
        )
    }
    val forumInfo = forumInfo?.let {
        ThreadForumInfo(
            name = it.name,
            avatar = it.avatar,
        )
    }
    val abstractSegments = if (richAbstract.isNotEmpty()) {
        richAbstract.map {
            RichTextSegment(
                type = it.type,
                text = it.text,
                c = it.c,
            )
        }
    } else {
        _abstract.map {
            RichTextSegment(
                type = it.type,
                text = it.text,
            )
        }
    }
    val medias = media.map {
        ThreadMediaItem(
            originPic = it.originPic,
            bigPic = it.bigPic,
            dynamicPic = it.dynamicPic,
            srcPic = it.srcPic,
            postId = it.postId,
            showOriginalBtn = it.showOriginalBtn,
            originSize = it.originSize,
        )
    }
    val videoInfo = videoInfo?.let {
        ThreadVideoInfo(
            videoUrl = it.videoUrl,
            thumbnailUrl = it.thumbnailUrl,
            thumbnailWidth = it.thumbnailWidth,
            thumbnailHeight = it.thumbnailHeight,
        )
    }
    val originThread = origin_thread_info?.toOriginThreadCard()
    return ThreadCard(
        threadId = threadId.takeIf { it != 0L } ?: id,
        firstPostId = firstPostId,
        forumId = forumId,
        forumName = forumName,
        title = title,
        tabName = tabName,
        isNoTitle = isNoTitle == 1,
        isGood = isGood == 1,
        isTop = isTop == 1,
        isShareThread = is_share_thread == 1,
        lastTimeInt = lastTimeInt,
        shareNum = shareNum.toInt(),
        replyNum = replyNum,
        hotNum = hotNum,
        agreeNum = agreeNum,
        hasAgree = agree?.hasAgree ?: 0,
        collectStatus = collectStatus,
        collectMarkPid = collectMarkPid.toLongOrNull() ?: 0L,
        author = author,
        forumInfo = forumInfo,
        abstractSegments = abstractSegments,
        medias = medias,
        videoInfo = videoInfo,
        hasOriginThreadInfo = originThread != null,
        originThreadPayload = originThread,
        authorId = authorId,
    )
}

fun FrsPageResponse.toForumPageData(): ForumPageData {
    val data = checkNotNull(data_) { "frsPage data is null" }
    val forum = checkNotNull(data.forum) { "frsPage forum is null" }
    val page = data.page
    val pageInfo = page?.let {
        ForumPageInfo(
            currentPage = it.current_page,
            totalPage = it.total_page,
            hasMore = it.has_more != 0 || it.current_page < it.total_page,
            hasPrev = it.has_prev != 0 || it.current_page > 1,
        )
    }
    return ForumPageData(
        forum = forum.toForumInfo(),
        tbs = data.anti?.tbs,
        page = pageInfo,
        threadList = data.thread_list.map { it.toThreadCard() },
        threadIdList = data.thread_id_list,
        forumRule = data.forum_rule?.toForumRuleSummary(),
    )
}

fun RecommendForumInfo.toForumDetailInfo(): ForumDetailInfo =
    ForumDetailInfo(
        forumId = forum_id,
        forumName = forum_name,
        avatar = avatar,
        memberCount = member_count.toInt(),
        threadCount = thread_count.toInt(),
        slogan = slogan,
        intro = content.map { it.toThreadContentItem() },
    )

fun BawuRoleInfoPub.toForumRuleAuthor(): ForumRuleAuthor =
    ForumRuleAuthor(
        userId = user_id,
        userName = user_name,
        nameShow = name_show,
        portrait = portrait,
        roleName = role_name,
        userLevel = user_level,
        levelName = level_name,
    )

fun ForumRule.toForumRuleItem(): ForumRuleItem =
    ForumRuleItem(
        title = title,
        content = content.map { it.toThreadContentItem() },
    )

fun ForumRuleDetailResponse.toForumRuleDetail(): ForumRuleDetail {
    val data = checkNotNull(data_) { "forumRuleDetail data is null" }
    return ForumRuleDetail(
        title = data.title,
        publishTime = data.publish_time,
        preface = data.preface,
        rules = data.rules.map { it.toForumRuleItem() },
        author = data.bazhu?.toForumRuleAuthor(),
    )
}
