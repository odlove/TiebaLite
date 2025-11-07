package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.renders
import com.huanchengfly.tieba.post.api.models.protos.contentRenders
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.models.mappers.ThreadMapper
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
                    intent.threadInfo?.title.orEmpty(),
                    intent.threadInfo?.author,
                    intent.threadInfo,
                    intent.threadInfo?.firstPostContent?.renders ?: emptyList(),
                    intent.postId,
                    intent.seeLz,
                    intent.sortType,
                    sanitizedMeta = intent.threadInfo?.let { ThreadMapper.fromProto(it).meta }
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
            from = intent.from.takeIf { it == ThreadPageFrom.FROM_STORE }.orEmpty()
        )
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
                ThreadPartialChange.Load.Success(
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
                    firstPost?.contentRenders,
                    intent.postId,
                    intent.seeLz,
                    intent.sortType,
                    threadId = intent.threadId,
                    postIds = allPosts.map { it.id }.toImmutableList(),
                )
            }
            .onStart { emit(ThreadPartialChange.Load.Start) }
            .catch { emit(ThreadPartialChange.Load.Failure(it)) }
}
