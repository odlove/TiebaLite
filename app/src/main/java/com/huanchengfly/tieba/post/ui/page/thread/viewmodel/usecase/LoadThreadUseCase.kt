package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.common.thread.ThreadUser
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.ThreadPageFrom
import com.huanchengfly.tieba.post.ui.page.thread.mapper.ThreadPostMapper
import com.huanchengfly.tieba.post.ui.page.thread.mapper.nextPagePostId
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@ViewModelScoped
class ThreadInitUseCase @Inject constructor() : ThreadIntentUseCase<ThreadUiIntent.Init> {
    override fun execute(intent: ThreadUiIntent.Init): Flow<ThreadPartialChange> =
        flow<ThreadPartialChange> {
            emit(
                ThreadPartialChange.Init.Success(
                    intent.threadDetail?.title.orEmpty(),
                    intent.threadDetail?.author,
                    intent.threadDetail,
                    emptyList(),
                    intent.postId,
                    intent.seeLz,
                    intent.sortType,
                    sanitizedMeta = intent.threadDetail?.let {
                        ThreadMeta(
                            hasAgree = it.agree?.hasAgree == 1,
                            agreeNum = it.agree?.agreeNum?.toInt() ?: 0,
                            collectStatus = it.collectStatus == 1,
                            collectMarkPid = it.collectMarkPid,
                            replyNum = it.replyNum
                        )
                    }
                )
            )
        }.catch { emit(ThreadPartialChange.Init.Failure(it)) }
}

@ViewModelScoped
class LoadThreadUseCase @Inject constructor(
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.Load> {
    override fun execute(intent: ThreadUiIntent.Load): Flow<ThreadPartialChange> =
        pbPageRepository.pbPage(
            intent.threadId,
            intent.page,
            intent.postId,
            intent.forumId,
            intent.seeLz,
            intent.sortType,
            from = intent.from.takeIf { it == ThreadPageFrom.FROM_COLLECT }.orEmpty()
        )
            .map { response ->
                val page = requireNotNull(response.page) { "pbPage page is null" }
                val forum = requireNotNull(response.forum) { "pbPage forum is null" }
                val anti = requireNotNull(response.anti) { "pbPage anti is null" }
                val threadDetail = response.thread
                val author = requireNotNull(threadDetail.author) { "pbPage author is null" }
                val currentUser = response.user ?: ThreadUser()
                val postList = response.posts
                val firstPost = response.firstPost
                val notFirstPosts = postList.filterNot { it.floor == 1 }
                val allPosts = listOfNotNull(firstPost) + notFirstPosts
                val threadAuthorId = threadDetail.author?.id
                ThreadPartialChange.Load.Success(
                    threadDetail.title,
                    author,
                    currentUser,
                    firstPost,
                    ThreadPostMapper.mapPosts(notFirstPosts, threadAuthorId),
                    threadDetail,
                    forum,
                    anti,
                    page.currentPage,
                    page.totalPage,
                    page.hasMore,
                    threadDetail.nextPagePostId(
                        postList.map { it.id },
                        intent.sortType
                    ),
                    page.hasPrev,
                    firstPost?.contentRenders,
                    intent.postId,
                    intent.seeLz,
                    intent.sortType,
                    threadId = intent.threadId,
                    postIds = allPosts.map { it.id }.toImmutableList(),
                ) as ThreadPartialChange
            }
            .onStart { emit(ThreadPartialChange.Load.Start) }
            .catch { emit(ThreadPartialChange.Load.Failure(it)) }
}
