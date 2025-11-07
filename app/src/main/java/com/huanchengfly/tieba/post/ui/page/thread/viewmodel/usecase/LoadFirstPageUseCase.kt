package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.contentRenders
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
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
class LoadFirstPageUseCase @Inject constructor(
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.LoadFirstPage> {
    override fun execute(intent: ThreadUiIntent.LoadFirstPage): Flow<ThreadPartialChange> =
        pbPageRepository.pbPage(intent.threadId, 0, 0, intent.forumId, intent.seeLz, intent.sortType)
            .map<PbPageResponse, ThreadPartialChange> { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val page = data.page ?: throw TiebaUnknownException
                val thread = data.thread ?: throw TiebaUnknownException
                val author = thread.author ?: throw TiebaUnknownException
                val forum = data.forum ?: throw TiebaUnknownException
                val anti = data.anti ?: throw TiebaUnknownException
                val postList = data.post_list
                val firstPost = data.first_floor_post
                val notFirstPosts = postList.filterNot { it.floor == 1 }
                val allPosts = listOfNotNull(firstPost) + notFirstPosts
                ThreadPartialChange.LoadFirstPage.Success(
                    thread.title,
                    author,
                    data.user ?: User(),
                    firstPost,
                    ThreadPostMapper.mapPosts(notFirstPosts),
                    thread,
                    forum,
                    anti,
                    page.current_page,
                    page.new_total_page,
                    page.has_more != 0,
                    thread.nextPagePostId(
                        postList.map { it.id },
                        intent.sortType
                    ),
                    page.has_prev != 0,
                    firstPost?.contentRenders ?: emptyList(),
                    postId = 0,
                    intent.seeLz,
                    intent.sortType,
                    threadId = intent.threadId,
                    postIds = allPosts.map { it.id }.toImmutableList(),
                )
            }
            .onStart { emit(ThreadPartialChange.LoadFirstPage.Start) }
            .catch { emit(ThreadPartialChange.LoadFirstPage.Failure(it)) }
}
