package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.network.error.getErrorCode
import com.huanchengfly.tieba.core.network.error.getErrorMessage
import com.huanchengfly.tieba.post.models.mappers.toThreadDetail
import com.huanchengfly.tieba.post.models.mappers.toThreadPost
import com.huanchengfly.tieba.post.models.mappers.toThreadUser
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
                val data = requireNotNull(response.data_) { "pbPage data is null" }
                val page = requireNotNull(data.page) { "pbPage page is null" }
                val thread = requireNotNull(data.thread) { "pbPage thread is null" }
                val author = requireNotNull(thread.author) { "pbPage author is null" }
                requireNotNull(data.forum) { "pbPage forum is null" }
                requireNotNull(data.anti) { "pbPage anti is null" }
                val postList = data.post_list
                val posts = postList.filterNot { it.floor == 1 || intent.postIds.contains(it.id) }
                    .map { it.toThreadPost() }
                val threadDetail = thread.toThreadDetail()
                val threadAuthorId = threadDetail.author?.id
                ThreadPartialChange.LoadMore.Success(
                    author.toThreadUser(),
                    ThreadPostMapper.mapPosts(posts, threadAuthorId),
                    threadDetail,
                    page.current_page,
                    page.new_total_page,
                    page.has_more != 0,
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
                val data = requireNotNull(response.data_) { "pbPage data is null" }
                val page = requireNotNull(data.page) { "pbPage page is null" }
                val thread = requireNotNull(data.thread) { "pbPage thread is null" }
                val author = requireNotNull(thread.author) { "pbPage author is null" }
                requireNotNull(data.forum) { "pbPage forum is null" }
                requireNotNull(data.anti) { "pbPage anti is null" }
                val postList = data.post_list
                val posts = postList.filterNot { it.floor == 1 || intent.postIds.contains(it.id) }
                    .map { it.toThreadPost() }
                val threadDetail = thread.toThreadDetail()
                val threadAuthorId = threadDetail.author?.id
                ThreadPartialChange.LoadPrevious.Success(
                    author.toThreadUser(),
                    ThreadPostMapper.mapPosts(posts, threadAuthorId),
                    threadDetail,
                    page.current_page,
                    page.new_total_page,
                    page.has_prev != 0,
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
