package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.network.error.getErrorCode
import com.huanchengfly.tieba.core.network.error.getErrorMessage
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.ui.page.thread.mapper.ThreadPostMapper
import com.huanchengfly.tieba.post.ui.page.thread.mapper.nextPagePostId
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@ViewModelScoped
class LoadMoreUseCase @Inject constructor(
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.LoadMore> {
    override fun execute(intent: ThreadUiIntent.LoadMore): Flow<ThreadPartialChange> =
        pbPageRepository.pbPage(
            intent.threadId,
            intent.page,
            intent.postId,
            intent.forumId,
            intent.seeLz,
            intent.sortType
        )
            .map { response ->
                val page = requireNotNull(response.page) { "pbPage page is null" }
                requireNotNull(response.forum) { "pbPage forum is null" }
                requireNotNull(response.anti) { "pbPage anti is null" }
                val threadDetail = response.thread
                val author = requireNotNull(threadDetail.author) { "pbPage author is null" }
                val postList = response.posts
                val posts = postList.filterNot { it.floor == 1 || intent.postIds.contains(it.id) }
                val threadAuthorId = threadDetail.author?.id
                ThreadPartialChange.LoadMore.Success(
                    author,
                    ThreadPostMapper.mapPosts(posts, threadAuthorId),
                    threadDetail,
                    page.currentPage,
                    page.totalPage,
                    page.hasMore,
                    threadDetail.nextPagePostId(
                        intent.postIds + posts.map { it.id },
                        intent.sortType
                    ),
                    newPostIds = posts.map { it.id }.toImmutableList(),
                ) as ThreadPartialChange
            }
            .onStart { emit(ThreadPartialChange.LoadMore.Start) }
            .catch {
                emit(
                    ThreadPartialChange.LoadMore.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
}

@ViewModelScoped
class LoadPreviousUseCase @Inject constructor(
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.LoadPrevious> {
    override fun execute(intent: ThreadUiIntent.LoadPrevious): Flow<ThreadPartialChange> =
        pbPageRepository.pbPage(
            intent.threadId,
            intent.page,
            intent.postId,
            intent.forumId,
            intent.seeLz,
            intent.sortType,
            back = true
        )
            .map { response ->
                val page = requireNotNull(response.page) { "pbPage page is null" }
                requireNotNull(response.forum) { "pbPage forum is null" }
                requireNotNull(response.anti) { "pbPage anti is null" }
                val threadDetail = response.thread
                val author = requireNotNull(threadDetail.author) { "pbPage author is null" }
                val postList = response.posts
                val posts = postList.filterNot { it.floor == 1 || intent.postIds.contains(it.id) }
                val threadAuthorId = threadDetail.author?.id
                ThreadPartialChange.LoadPrevious.Success(
                    author,
                    ThreadPostMapper.mapPosts(posts, threadAuthorId),
                    threadDetail,
                    page.currentPage,
                    page.totalPage,
                    page.hasPrev,
                    newPostIds = posts.map { it.id }.toImmutableList(),
                ) as ThreadPartialChange
            }
            .onStart { emit(ThreadPartialChange.LoadPrevious.Start) }
            .catch {
                emit(
                    ThreadPartialChange.LoadPrevious.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
}
