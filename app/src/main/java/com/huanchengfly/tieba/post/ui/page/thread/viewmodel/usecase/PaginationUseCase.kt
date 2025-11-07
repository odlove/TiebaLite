package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
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
            .map<PbPageResponse, ThreadPartialChange> { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val page = data.page ?: throw TiebaUnknownException
                val thread = data.thread ?: throw TiebaUnknownException
                val author = thread.author ?: throw TiebaUnknownException
                data.forum ?: throw TiebaUnknownException
                data.anti ?: throw TiebaUnknownException
                val postList = data.post_list
                val posts = postList.filterNot { it.floor == 1 || intent.postIds.contains(it.id) }
                ThreadPartialChange.LoadMore.Success(
                    author,
                    ThreadPostMapper.mapPosts(posts),
                    thread,
                    page.current_page,
                    page.new_total_page,
                    page.has_more != 0,
                    thread.nextPagePostId(
                        intent.postIds + posts.map { it.id },
                        intent.sortType
                    ),
                    newPostIds = posts.map { it.id }.toImmutableList(),
                )
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
            .map<PbPageResponse, ThreadPartialChange> { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val page = data.page ?: throw TiebaUnknownException
                val thread = data.thread ?: throw TiebaUnknownException
                val author = thread.author ?: throw TiebaUnknownException
                data.forum ?: throw TiebaUnknownException
                data.anti ?: throw TiebaUnknownException
                val postList = data.post_list
                val posts = postList.filterNot { it.floor == 1 || intent.postIds.contains(it.id) }
                ThreadPartialChange.LoadPrevious.Success(
                    author,
                    ThreadPostMapper.mapPosts(posts),
                    thread,
                    page.current_page,
                    page.new_total_page,
                    page.has_prev != 0,
                    newPostIds = posts.map { it.id }.toImmutableList(),
                )
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
