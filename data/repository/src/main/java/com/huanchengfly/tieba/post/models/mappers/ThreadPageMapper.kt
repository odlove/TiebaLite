package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.feed.OriginThreadCard
import com.huanchengfly.tieba.core.common.feed.RichTextSegment
import com.huanchengfly.tieba.core.common.feed.ThreadMediaItem
import com.huanchengfly.tieba.core.common.feed.ThreadVideoInfo
import com.huanchengfly.tieba.core.common.thread.ThreadAgree
import com.huanchengfly.tieba.core.common.thread.ThreadAnti
import com.huanchengfly.tieba.core.common.thread.ThreadContentItem
import com.huanchengfly.tieba.core.common.thread.ThreadDetail
import com.huanchengfly.tieba.core.common.thread.ThreadForum
import com.huanchengfly.tieba.core.common.thread.ThreadPageData
import com.huanchengfly.tieba.core.common.thread.ThreadPageInfo
import com.huanchengfly.tieba.core.common.thread.ThreadPost
import com.huanchengfly.tieba.core.common.thread.SubPostsPageData
import com.huanchengfly.tieba.core.common.thread.SubPostsPageInfo
import com.huanchengfly.tieba.core.common.thread.ThreadSubPost
import com.huanchengfly.tieba.core.common.thread.ThreadUser
import com.huanchengfly.tieba.post.api.models.protos.Agree
import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.PbContent
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse

fun User.toThreadUser(): ThreadUser {
    val bawuType = if (is_bawu == 1) {
        if (bawu_type == "manager") "吧主" else "小吧主"
    } else {
        null
    }
    return ThreadUser(
        id = id,
        name = name,
        nameShow = nameShow,
        portrait = portrait,
        levelId = level_id,
        bawuType = bawuType,
        ipAddress = ip_address,
        isLogin = is_login,
    )
}

fun SimpleForum.toThreadForum(): ThreadForum =
    ThreadForum(
        id = id,
        name = name,
        avatar = avatar,
    )

fun Anti.toThreadAnti(): ThreadAnti = ThreadAnti(tbs = tbs)

fun Agree.toThreadAgree(): ThreadAgree =
    ThreadAgree(
        hasAgree = hasAgree,
        agreeNum = agreeNum,
        diffAgreeNum = diffAgreeNum,
    )

fun ThreadInfo.toThreadDetail(): ThreadDetail {
    val postIds = pids.split(",")
        .filterNot { it.isBlank() }
        .mapNotNull { it.toLongOrNull() }
    return ThreadDetail(
        threadId = id,
        firstPostId = firstPostId,
        title = title,
        replyNum = replyNum,
        forumId = forumId,
        forumName = forumName,
        isShareThread = is_share_thread == 1,
        author = author?.toThreadUser(),
        agree = ThreadAgree(
            hasAgree = agree?.hasAgree ?: 0,
            agreeNum = agreeNum.toLong(),
            diffAgreeNum = agree?.diffAgreeNum ?: 0L,
        ),
        collectStatus = collectStatus,
        collectMarkPid = collectMarkPid.toLongOrNull() ?: 0L,
        postIds = postIds,
        originThread = origin_thread_info?.toOriginThreadCard(),
    )
}

fun PbContent.toThreadContentItem(): ThreadContentItem =
    ThreadContentItem(
        type = type,
        text = text,
        link = link,
        c = c,
        bsize = bsize,
        bigSrc = bigSrc,
        bigCdnSrc = bigCdnSrc,
        originSrc = originSrc,
        showOriginalBtn = showOriginalBtn,
        originSize = originSize,
        uid = uid,
        dynamicUrl = dynamic_,
        width = width.toInt(),
        height = height.toInt(),
        cdnSrcActive = cdnSrcActive,
        cdnSrc = cdnSrc,
        src = src,
        voiceMd5 = voiceMD5,
        duringTime = duringTime,
    )

fun List<PbContent>.toThreadContentItems(): List<ThreadContentItem> =
    map { it.toThreadContentItem() }

fun SubPostList.toThreadSubPost(): ThreadSubPost =
    ThreadSubPost(
        id = id,
        time = time.toLong(),
        authorId = author_id,
        author = author?.toThreadUser(),
        content = content.toThreadContentItems(),
        agree = agree?.toThreadAgree(),
    )

fun Post.toThreadPost(): ThreadPost {
    val subPosts = sub_post_list?.sub_post_list.orEmpty().map { it.toThreadSubPost() }
    return ThreadPost(
        id = id,
        threadId = tid,
        authorId = author_id,
        floor = floor,
        title = title,
        isNTitle = is_ntitle,
        time = time.toLong(),
        author = author?.toThreadUser(),
        content = content.toThreadContentItems(),
        agree = agree?.toThreadAgree(),
        subPostCount = sub_post_number,
        subPosts = subPosts,
        forum = from_forum?.toThreadForum(),
    )
}

fun OriginThreadInfo.toOriginThreadCard(): OriginThreadCard {
    val abstractSegments = _abstract.map {
        RichTextSegment(
            type = it.type,
            text = it.text,
        )
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
    val videoInfo = video_info?.let {
        ThreadVideoInfo(
            videoUrl = it.videoUrl,
            thumbnailUrl = it.thumbnailUrl,
            thumbnailWidth = it.thumbnailWidth,
            thumbnailHeight = it.thumbnailHeight,
        )
    }
    return OriginThreadCard(
        threadId = tid.toLongOrNull() ?: 0L,
        forumId = fid,
        forumName = fname,
        title = title,
        abstractSegments = abstractSegments,
        medias = medias,
        videoInfo = videoInfo,
    )
}

fun PbPageResponse.toThreadPageData(): ThreadPageData {
    val data = checkNotNull(data_)
    val thread = checkNotNull(data.thread).toThreadDetail()
    val forum = data.forum?.toThreadForum()
    val user = data.user?.toThreadUser()
    val anti = data.anti?.toThreadAnti()
    val page = data.page?.let {
        ThreadPageInfo(
            currentPage = it.current_page,
            totalPage = it.new_total_page,
            hasMore = it.has_more != 0,
            hasPrev = it.has_prev != 0,
        )
    }
    val posts = data.post_list.map { it.toThreadPost() }
    val firstPost = data.first_floor_post?.toThreadPost()
    return ThreadPageData(
        thread = thread,
        forum = forum,
        user = user,
        anti = anti,
        page = page,
        posts = posts,
        firstPost = firstPost,
    )
}

fun PbFloorResponse.toSubPostsPageData(): SubPostsPageData {
    val data = checkNotNull(data_) { "pbFloor data is null" }
    val page = checkNotNull(data.page) { "pbFloor page is null" }
    val thread = checkNotNull(data.thread) { "pbFloor thread is null" }
    val forum = checkNotNull(data.forum) { "pbFloor forum is null" }
    val post = checkNotNull(data.post) { "pbFloor post is null" }
    val anti = checkNotNull(data.anti) { "pbFloor anti is null" }
    return SubPostsPageData(
        thread = thread.toThreadDetail(),
        forum = forum.toThreadForum(),
        post = post.toThreadPost(),
        anti = anti.toThreadAnti(),
        page = SubPostsPageInfo(
            currentPage = page.current_page,
            totalPage = page.total_page,
            totalCount = page.total_count,
            hasMore = page.has_more != 0 || page.current_page < page.total_page,
            hasPrev = page.has_prev != 0 || page.current_page > 1,
        ),
        subPosts = data.subpost_list.map { it.toThreadSubPost() },
    )
}
