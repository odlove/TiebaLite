package com.huanchengfly.tieba.post.ui.page.search.mapper

import com.huanchengfly.tieba.core.common.search.SearchForum
import com.huanchengfly.tieba.core.common.search.SearchMainPost
import com.huanchengfly.tieba.core.common.search.SearchMedia
import com.huanchengfly.tieba.core.common.search.SearchPost
import com.huanchengfly.tieba.core.common.search.SearchThreadItem
import com.huanchengfly.tieba.core.common.search.SearchUser
import com.huanchengfly.tieba.post.api.models.SearchThreadBean

fun SearchThreadBean.ThreadInfoBean.toSearchThreadItem(): SearchThreadItem =
    SearchThreadItem(
        threadId = tid,
        postId = pid,
        subPostId = cid,
        user = user.toSearchUser(),
        time = time,
        title = title,
        content = content,
        mainPost = mainPost?.toSearchMainPost(),
        quotePost = postInfo?.toSearchPost(),
        media = media.map { it.toSearchMedia() },
        forumName = forumName,
        forumInfo = forumInfo.toSearchForum(),
        postNum = postNum,
        likeNum = likeNum,
        shareNum = shareNum,
    )

fun SearchThreadBean.UserInfoBean.toSearchUser(): SearchUser =
    SearchUser(
        userId = userId,
        userName = userName.orEmpty(),
        showNickname = showNickname,
        portrait = portrait,
    )

fun SearchThreadBean.ForumInfo.toSearchForum(): SearchForum =
    SearchForum(
        name = forumName,
        avatar = avatar,
    )

fun SearchThreadBean.MediaInfo.toSearchMedia(): SearchMedia =
    SearchMedia(
        type = type,
        bigPic = bigPic,
        smallPic = smallPic,
        waterPic = waterPic,
    )

fun SearchThreadBean.PostInfo.toSearchPost(): SearchPost =
    SearchPost(
        user = user.toSearchUser(),
        content = content,
        threadId = tid,
        postId = pid,
    )

fun SearchThreadBean.MainPost.toSearchMainPost(): SearchMainPost =
    SearchMainPost(
        user = user.toSearchUser(),
        title = title,
        content = content,
        threadId = tid,
    )
