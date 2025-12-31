package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.core.common.forum.ForumInfo
import com.huanchengfly.tieba.core.common.forum.ForumLikeResult
import com.huanchengfly.tieba.core.common.forum.ForumPageData
import com.huanchengfly.tieba.core.common.forum.ForumPageInfo
import com.huanchengfly.tieba.core.common.forum.ForumSignResult
import com.huanchengfly.tieba.core.common.hottopic.HotTopicItem
import com.huanchengfly.tieba.core.common.reply.AddPostResult
import com.huanchengfly.tieba.core.common.search.SearchThreadResult
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import io.mockk.every
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object TestFixtures {

    fun fakeFrsPageResponse(): ForumPageData =
        ForumPageData(
            forum = ForumInfo(),
            page = ForumPageInfo(hasMore = true),
            threadList = emptyList(),
            threadIdList = emptyList()
        )

    fun fakeCommonResponse(errorCode: Int = 0, errorMsg: String = "success") =
        CommonResponse(errorCode = errorCode, errorMsg = errorMsg)

    fun fakeForumSignResult(): ForumSignResult =
        ForumSignResult(
            signBonusPoint = 10,
            levelUpScore = 100,
            contSignNum = 1,
            userSignRank = 1,
            isSignIn = 1,
            level = 1,
            levelName = "Lv1"
        )

    fun fakeForumLikeResult(): ForumLikeResult =
        ForumLikeResult(
            memberSum = "0",
            curScore = 0,
            levelUpScore = 0,
            levelId = 0,
            levelName = ""
        )

    fun fakeAddPostResult(
        threadId: Long = 123456L,
        postId: Long = 789012L,
        expInc: String = "10"
    ): AddPostResult =
        AddPostResult(
            threadId = threadId,
            postId = postId,
            expInc = expInc
        )

    fun fakeSearchSuggestions(): List<String> = emptyList()

    fun fakeSearchThreadResult(): SearchThreadResult =
        SearchThreadResult(
            items = emptyList(),
            hasMore = true
        )

    fun fakeHotTopicList(): List<HotTopicItem> =
        listOf(
            HotTopicItem(
                topicId = 1L,
                topicName = "Hot Topic",
                topicDesc = "Hot Topic Desc",
                discussNum = 100L,
                topicImage = "https://example.com/topic.jpg",
                topicTag = 2
            )
        )

    fun mockFrsPageSuccess(
        repo: FrsPageRepository,
        response: ForumPageData = fakeFrsPageResponse()
    ): Flow<ForumPageData> {
        val flow = flowOf(response)
        every { repo.frsPage(any(), any(), any(), any(), any(), any()) } returns flow
        return flow
    }

    fun mockSignSuccess(
        repo: ForumOperationRepository,
        forumId: String,
        forumName: String,
        tbs: String,
        response: ForumSignResult = fakeForumSignResult()
    ): Flow<ForumSignResult> {
        val flow = flowOf(response)
        every { repo.sign(forumId, forumName, tbs) } returns flow
        return flow
    }

    fun mockLikeForumSuccess(
        repo: ForumOperationRepository,
        forumId: String,
        forumName: String,
        tbs: String,
        response: ForumLikeResult = fakeForumLikeResult()
    ): Flow<ForumLikeResult> {
        val flow = flowOf(response)
        every { repo.likeForum(forumId, forumName, tbs) } returns flow
        return flow
    }

    fun mockUnlikeForumSuccess(
        repo: ForumOperationRepository,
        forumId: String,
        forumName: String,
        tbs: String,
        response: CommonResponse = fakeCommonResponse()
    ): Flow<CommonResponse> {
        val flow = flowOf(response)
        every { repo.unlikeForum(forumId, forumName, tbs) } returns flow
        return flow
    }
}
