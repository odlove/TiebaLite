package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.LikeForumResultBean
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.api.models.protos.searchSug.SearchSugResponse
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Shared helper functions used by unit tests.
 *
 * Keeps mock/stub creation in a single place so individual tests stay small and focused.
 */
object TestFixtures {

    fun fakeFrsPageResponse(): FrsPageResponse = mockk(relaxed = true)

    fun fakeThreadListResponse(): ThreadListResponse = mockk(relaxed = true)

    fun fakeCommonResponse(errorCode: Int = 0, errorMsg: String = "success") =
        CommonResponse(errorCode = errorCode, errorMsg = errorMsg)

    fun fakeAgreeBean(
        errorCode: String = "0",
        errorMsg: String = "success",
        score: String? = null
    ): AgreeBean =
        mockk(relaxed = true) {
            every { this@mockk.errorCode } returns errorCode
            every { this@mockk.errorMsg } returns errorMsg
            if (score != null) {
                every { this@mockk.data } returns mockk(relaxed = true) {
                    every { agree } returns mockk(relaxed = true) {
                        every { this@mockk.score } returns score
                    }
                }
            }
        }

    fun fakeMockAgreeBean(
        errorCode: String = "0",
        errorMsg: String = "success",
        score: String = "0"
    ): AgreeBean = fakeAgreeBean(errorCode, errorMsg, score)

    fun fakeSignResultBean(
        errorCode: String = "0",
        errorMsg: String = "success"
    ): SignResultBean = SignResultBean(errorCode = errorCode, errorMsg = errorMsg)

    fun fakeLikeForumResultBean(
        errorCode: String = "0",
        errno: String = "0",
        errmsg: String = "",
        usermsg: String = "",
        curScore: String = "",
        levelUpScore: String = "",
        levelId: String = "",
        levelName: String = "",
        memberSum: String = "",
        permLevelId: String = "",
        permLevelName: String = ""
    ): LikeForumResultBean = LikeForumResultBean(
        errorCode = errorCode,
        error = LikeForumResultBean.ErrorInfo(errno = errno, errmsg = errmsg, usermsg = usermsg),
        info = LikeForumResultBean.Info(
            curScore = curScore,
            levelUpScore = levelUpScore,
            levelId = levelId,
            levelName = levelName,
            memberSum = memberSum
        ),
        userPerm = LikeForumResultBean.UserPermInfo(
            levelId = permLevelId,
            levelName = permLevelName
        )
    )

    fun fakeMessageListBean(): MessageListBean = mockk(relaxed = true) {
        every { page } returns mockk(relaxed = true) {
            every { hasMore } returns "1"
        }
        every { replyList } returns emptyList()
        every { atList } returns emptyList()
    }

    fun fakeAddPostResponse(
        tid: String = "123456",
        pid: String = "789012",
        expInc: String = "10"
    ): AddPostResponse = mockk(relaxed = true) {
        every { data_ } returns mockk(relaxed = true) {
            every { this@mockk.tid } returns tid
            every { this@mockk.pid } returns pid
            every { exp } returns mockk(relaxed = true) {
                every { inc } returns expInc
            }
        }
    }

    fun fakePersonalizedResponse(): PersonalizedResponse = mockk(relaxed = true) {
        every { data_ } returns mockk(relaxed = true) {
            every { thread_list } returns emptyList()
            every { thread_personalized } returns emptyList()
        }
    }

    fun fakeHotThreadListResponse(): HotThreadListResponse = mockk(relaxed = true) {
        every { data_ } returns mockk(relaxed = true) {
            every { topicList } returns emptyList()
            every { hotThreadTabInfo } returns emptyList()
            every { threadInfo } returns emptyList()
        }
    }

    fun fakeUserLikeResponse(): UserLikeResponse = mockk(relaxed = true) {
        every { data_ } returns mockk(relaxed = true) {
            every { threadInfo } returns emptyList()
            every { hasMore } returns 1
            every { pageTag } returns ""
            every { requestUnix } returns 0L
        }
    }

    fun fakeSearchSugResponse(): SearchSugResponse = mockk(relaxed = true) {
        every { data_ } returns mockk(relaxed = true) {
            every { list } returns emptyList()
            every { forum_list } returns emptyList()
        }
    }

    fun fakeSearchThreadBean(): SearchThreadBean = mockk(relaxed = true) {
        every { data } returns mockk(relaxed = true) {
            every { postList } returns emptyList()
            every { hasMore } returns 1
        }
    }

    fun fakeTopicListResponse(): TopicListResponse = mockk(relaxed = true) {
        every { data_ } returns mockk(relaxed = true) {
            every { topic_list } returns emptyList()
        }
    }

    fun mockFrsPageSuccess(
        repo: FrsPageRepository,
        response: FrsPageResponse = fakeFrsPageResponse()
    ): Flow<FrsPageResponse> {
        val flow = flowOf(response)
        every { repo.frsPage(any(), any(), any(), any(), any(), any()) } returns flow
        return flow
    }

    fun mockThreadListSuccess(
        repo: FrsPageRepository,
        response: ThreadListResponse = fakeThreadListResponse()
    ): Flow<ThreadListResponse> {
        val flow = flowOf(response)
        every { repo.threadList(any(), any(), any(), any(), any()) } returns flow
        return flow
    }

    fun mockSignSuccess(
        repo: ForumOperationRepository,
        forumId: String,
        forumName: String,
        tbs: String,
        response: SignResultBean = fakeSignResultBean()
    ): Flow<SignResultBean> {
        val flow = flowOf(response)
        every { repo.sign(forumId, forumName, tbs) } returns flow
        return flow
    }

    fun mockLikeForumSuccess(
        repo: ForumOperationRepository,
        forumId: String,
        forumName: String,
        tbs: String,
        response: LikeForumResultBean = fakeLikeForumResultBean()
    ): Flow<LikeForumResultBean> {
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
