package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.common.thread.ThreadUser
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
                ThreadPartialChange.LoadFirstPage.Success(
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
                    firstPost?.contentRenders ?: emptyList(),
                    postId = 0,
                    intent.seeLz,
                    intent.sortType,
                    threadId = intent.threadId,
                    postIds = allPosts.map { it.id }.toImmutableList(),
                ) as ThreadPartialChange
            }
            .onStart { emit(ThreadPartialChange.LoadFirstPage.Start) }
            .catch { emit(ThreadPartialChange.LoadFirstPage.Failure(it)) }
}
