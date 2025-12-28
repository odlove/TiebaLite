package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.search.SearchForum
import com.huanchengfly.tieba.core.common.search.SearchForumResult
import com.huanchengfly.tieba.core.common.search.SearchMainPost
import com.huanchengfly.tieba.core.common.search.SearchMedia
import com.huanchengfly.tieba.core.common.search.SearchPost
import com.huanchengfly.tieba.core.common.search.SearchThreadItem
import com.huanchengfly.tieba.core.common.search.SearchThreadResult
import com.huanchengfly.tieba.core.common.search.SearchUser
import com.huanchengfly.tieba.core.common.search.SearchUserResult
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import com.huanchengfly.tieba.post.api.models.protos.searchSug.SearchSugResponse

fun SearchThreadBean.toSearchThreadResult(): SearchThreadResult =
    SearchThreadResult(
        items = data.postList.map { it.toSearchThreadItem() },
        hasMore = data.hasMore == 1
    )

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

fun SearchUserBean.toSearchUserResult(): SearchUserResult =
    SearchUserResult(
        exactMatch = data?.exactMatch?.toSearchUser(),
        fuzzyMatch = data?.fuzzyMatch?.map { it.toSearchUser() } ?: emptyList(),
    )

fun SearchUserBean.UserBean.toSearchUser(): SearchUser =
    SearchUser(
        userId = id.orEmpty(),
        userName = name.orEmpty(),
        showNickname = showNickname,
        portrait = portrait,
        intro = intro,
    )

fun SearchForumBean.toSearchForumResult(): SearchForumResult =
    SearchForumResult(
        exactMatch = data?.exactMatch?.toSearchForum(),
        fuzzyMatch = data?.fuzzyMatch?.map { it.toSearchForum() } ?: emptyList(),
    )

fun SearchForumBean.ForumInfoBean.toSearchForum(): SearchForum =
    SearchForum(
        name = forumName.orEmpty(),
        nameShow = forumNameShow,
        avatar = avatar,
        slogan = slogan,
        intro = intro,
    )

fun SearchSugResponse.toSuggestionList(): List<String> = data_?.list ?: emptyList()
