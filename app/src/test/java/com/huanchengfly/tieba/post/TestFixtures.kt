package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.forum.ForumInfo
import com.huanchengfly.tieba.core.common.forum.ForumLikeResult
import com.huanchengfly.tieba.core.common.forum.ForumPageData
import com.huanchengfly.tieba.core.common.forum.ForumPageInfo
import com.huanchengfly.tieba.core.common.forum.ForumSignResult
import com.huanchengfly.tieba.core.common.search.SearchThreadResult
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.models.protos.Abstract
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.PbContent
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.core.common.reply.AddPostResult
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.api.models.protos.searchSug.SearchSugResponse
import com.huanchengfly.tieba.core.common.hottopic.HotTopicItem
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.models.PostMeta
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

    fun fakeFrsPageResponse(): ForumPageData =
        ForumPageData(
            forum = ForumInfo(),
            page = ForumPageInfo(hasMore = true),
            threadList = emptyList(),
            threadIdList = emptyList()
        )

    fun fakeThreadListResponse(): List<ThreadCard> = emptyList()

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

    fun fakeMessageListBean(): MessageListBean = mockk(relaxed = true) {
        every { page } returns mockk(relaxed = true) {
            every { hasMore } returns "1"
        }
        every { replyList } returns emptyList()
        every { atList } returns emptyList()
    }

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

    fun fakeSearchSuggestions(): List<String> = emptyList()

    fun fakeSearchThreadBean(): SearchThreadBean = mockk(relaxed = true) {
        every { data } returns mockk(relaxed = true) {
            every { postList } returns emptyList()
            every { hasMore } returns 1
        }
    }

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

    /**
     * Creates a mock PbPageResponse for testing
     *
     * @param thread Thread info
     * @param firstPost First floor post
     * @param posts List of posts
     * @param currentPage Current page number
     * @param totalPage Total page count
     * @param hasMore Whether has more pages
     * @param hasPrev Whether has previous pages
     * @return Mock PbPageResponse instance
     */
    fun fakePbPageResponse(
        thread: com.huanchengfly.tieba.post.api.models.protos.ThreadInfo? = fakeThreadInfo(),
        firstPost: com.huanchengfly.tieba.post.api.models.protos.Post? = fakePost(floor = 1),
        posts: List<com.huanchengfly.tieba.post.api.models.protos.Post> = emptyList(),
        currentPage: Int = 1,
        totalPage: Int = 1,
        hasMore: Int = 0,
        hasPrev: Int = 0,
        user: User = fakeUser(),
        forum: com.huanchengfly.tieba.post.api.models.protos.SimpleForum? = null,
        anti: com.huanchengfly.tieba.post.api.models.protos.Anti? = null
    ): com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse = mockk(relaxed = true) {
        every { data_ } returns mockk(relaxed = true) {
            every { this@mockk.thread } returns thread
            every { first_floor_post } returns firstPost
            every { post_list } returns posts
            every { page } returns mockk(relaxed = true) {
                every { current_page } returns currentPage
                every { new_total_page } returns totalPage
                every { has_more } returns hasMore
                every { has_prev } returns hasPrev
            }
            every { this@mockk.user } returns user
            every { this@mockk.forum } returns (forum ?: mockk<com.huanchengfly.tieba.post.api.models.protos.SimpleForum>(relaxed = true) {
                every { this@mockk.id } returns 789L
                every { this@mockk.name } returns "Test Forum"
            })
            every { this@mockk.anti } returns (anti ?: mockk<com.huanchengfly.tieba.post.api.models.protos.Anti>(relaxed = true) {
                every { this@mockk.tbs } returns "test_tbs"
            })
        }
    }

    fun mockFrsPageSuccess(
        repo: FrsPageRepository,
        response: ForumPageData = fakeFrsPageResponse()
    ): Flow<ForumPageData> {
        val flow = flowOf(response)
        every { repo.frsPage(any(), any(), any(), any(), any(), any()) } returns flow
        return flow
    }

    fun mockThreadListSuccess(
        repo: FrsPageRepository,
        response: List<ThreadCard> = fakeThreadListResponse()
    ): Flow<List<ThreadCard>> {
        val flow = flowOf(response)
        every { repo.threadList(any(), any(), any(), any(), any()) } returns flow
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

    // Mock factories for SubPosts testing

    /**
     * Creates a mock User for testing
     *
     * @param id User ID
     * @param name User name
     * @param nameShow User display name
     * @param portrait User portrait
     * @return Mock User instance
     */
    fun fakeUser(
        id: Long = 123456L,
        name: String = "testUser",
        nameShow: String = "Test User",
        portrait: String = "test_portrait"
    ): User = mockk(relaxed = true) {
        every { this@mockk.id } returns id
        every { this@mockk.name } returns name
        every { this@mockk.nameShow } returns nameShow
        every { this@mockk.portrait } returns portrait
    }

    /**
     * Creates a mock SubPostList for testing
     *
     * @param id Sub-post ID
     * @param authorId Author ID
     * @param author Author User object
     * @return Mock SubPostList instance
     */
    fun fakeSubPostList(
        id: Long = 789012L,
        authorId: Long = 123456L,
        author: User? = fakeUser(id = authorId)
    ): SubPostList = mockk(relaxed = true) {
        every { this@mockk.id } returns id
        every { author_id } returns authorId
        every { this@mockk.author } returns author
    }

    // Mock factories for Store testing

    /**
     * Creates a mock ThreadInfo for testing
     *
     * @param id Thread ID (field 1)
     * @param threadId Thread ID (field 2)
     * @param title Thread title
     * @param replyNum Reply count
     * @param agreeNum Total agree count (top-level field)
     * @param agree Agree object (contains hasAgree)
     * @return Mock ThreadInfo instance
     */
    fun fakeThreadInfo(
        id: Long = 123456L,
        threadId: Long = 123456L,
        firstPostId: Long = 654321L,
        title: String = "Test Thread",
        replyNum: Int = 100,
        viewNum: Int = 1000,
        createTime: Int = 1609459200,
        lastTimeInt: Int = 1609545600,
        isTop: Int = 0,
        isGood: Int = 0,
        isDeleted: Int = 0,
        author: User? = fakeUser(),
        authorId: Long = 123456L,
        forumId: Long = 789L,
        forumName: String = "Test Forum",
        agreeNum: Int = 50,
        hasAgree: Int = 0,
        collectStatus: Int = 0,
        collectMarkPid: String = ""
    ): com.huanchengfly.tieba.post.api.models.protos.ThreadInfo = mockk(relaxed = true) {
        every { this@mockk.id } returns id
        every { this@mockk.threadId } returns threadId
        every { this@mockk.firstPostId } returns firstPostId
        every { this@mockk.title } returns title
        every { this@mockk.replyNum } returns replyNum
        every { this@mockk.viewNum } returns viewNum
        every { this@mockk.createTime } returns createTime
        every { this@mockk.lastTimeInt } returns lastTimeInt
        every { this@mockk.isTop } returns isTop
        every { this@mockk.isGood } returns isGood
        every { this@mockk.isDeleted } returns isDeleted
        every { this@mockk.author } returns author
        every { this@mockk.authorId } returns authorId
        every { this@mockk.forumId } returns forumId
        every { this@mockk.forumName } returns forumName
        every { this@mockk._abstract } returns emptyList()
        every { media } returns emptyList()
        every { videoInfo } returns null
        every { this@mockk.agreeNum } returns agreeNum
        every { agree } returns mockk(relaxed = true) {
            every { this@mockk.hasAgree } returns hasAgree
            every { this@mockk.agreeNum } returns agreeNum.toLong()
            every { diffAgreeNum } returns agreeNum.toLong()
        }
        every { this@mockk.collectStatus } returns collectStatus
        every { this@mockk.collectMarkPid } returns collectMarkPid
    }

    /**
     * Creates a mock Post for testing
     *
     * @param id Post ID
     * @param threadId Thread ID
     * @param floor Floor number
     * @param time Post time
     * @param author Author
     * @param authorId Author ID
     * @param hasAgree Has user agreed (0/1)
     * @param diffAgreeNum Net agree count
     * @return Mock Post instance
     */
    fun fakePost(
        id: Long = 654321L,
        threadId: Long = 123456L,
        floor: Int = 1,
        time: Int = 1609459200,
        author: User? = fakeUser(),
        authorId: Long = 123456L,
        hasAgree: Int = 0,
        diffAgreeNum: Long = 10L,
        subPostNumber: Int = 0
    ): com.huanchengfly.tieba.post.api.models.protos.Post = mockk(relaxed = true) {
        every { this@mockk.id } returns id
        every { tid } returns threadId
        every { this@mockk.floor } returns floor
        every { this@mockk.time } returns time
        every { content } returns emptyList()
        every { this@mockk.author } returns author
        every { author_id } returns authorId
        every { sub_post_number } returns subPostNumber
        every { agree } returns mockk(relaxed = true) {
            every { this@mockk.hasAgree } returns hasAgree
            every { this@mockk.diffAgreeNum } returns diffAgreeNum
            every { agreeNum } returns diffAgreeNum
        }
    }

    /**
     * Creates a PostEntity for testing
     *
     * @param id Post ID
     * @param threadId Thread ID
     * @param floor Floor number
     * @param hasAgree Has user agreed
     * @param agreeNum Agree count
     * @return PostEntity instance
     */
    fun fakePostEntity(
        id: Long = 654321L,
        threadId: Long = 123456L,
        floor: Int = 1,
        time: Int = 1609459200,
        author: User? = fakeUser(),
        authorId: Long = 123456L,
        hasAgree: Int = 0,
        agreeNum: Int = 10,
        subPostNumber: Int = 0,
        timestamp: Long = 1000L
    ): PostEntity {
        val proto = fakePost(
            id = id,
            threadId = threadId,
            floor = floor,
            time = time,
            author = author,
            authorId = authorId,
            hasAgree = hasAgree,
            diffAgreeNum = agreeNum.toLong(),
            subPostNumber = subPostNumber
        )

        return PostEntity(
            id = id,
            threadId = threadId,
            floor = floor,
            time = time,
            author = author,
            authorId = authorId,
            content = emptyList<PbContent>(),
            subPostNumber = subPostNumber,
            meta = PostMeta(
                hasAgree = hasAgree,
                agreeNum = agreeNum
            ),
            proto = proto,
            timestamp = timestamp
        )
    }
}
