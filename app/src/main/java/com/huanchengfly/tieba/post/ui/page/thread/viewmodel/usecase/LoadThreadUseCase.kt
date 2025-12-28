package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.common.thread.ThreadUser
import com.huanchengfly.tieba.post.models.mappers.toThreadAnti
import com.huanchengfly.tieba.post.models.mappers.toThreadDetail
import com.huanchengfly.tieba.post.models.mappers.toThreadForum
import com.huanchengfly.tieba.post.models.mappers.toThreadPost
import com.huanchengfly.tieba.post.models.mappers.toThreadUser
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
                        com.huanchengfly.tieba.post.models.ThreadMeta(
                            hasAgree = it.agree?.hasAgree ?: 0,
                            agreeNum = it.agree?.agreeNum?.toInt() ?: 0,
                            collectStatus = it.collectStatus,
                            collectMarkPid = it.collectMarkPid
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
            from = intent.from.takeIf { it == ThreadPageFrom.FROM_STORE }.orEmpty()
        )
            .map { response ->
                val data = requireNotNull(response.data_) { "pbPage data is null" }
                val page = requireNotNull(data.page) { "pbPage page is null" }
                val thread = requireNotNull(data.thread) { "pbPage thread is null" }
                val author = requireNotNull(thread.author) { "pbPage author is null" }
                val forum = requireNotNull(data.forum) { "pbPage forum is null" }
                val anti = requireNotNull(data.anti) { "pbPage anti is null" }
                val postList = data.post_list
                val firstPost = data.first_floor_post?.toThreadPost()
                val notFirstPosts = postList.filterNot { it.floor == 1 }
                    .map { it.toThreadPost() }
                val allPosts = listOfNotNull(firstPost) + notFirstPosts
                val threadDetail = thread.toThreadDetail()
                val currentUser = data.user?.toThreadUser() ?: ThreadUser()
                val threadAuthorId = threadDetail.author?.id
                ThreadPartialChange.Load.Success(
                    thread.title,
                    author.toThreadUser(),
                    currentUser,
                    firstPost,
                    ThreadPostMapper.mapPosts(notFirstPosts, threadAuthorId),
                    threadDetail,
                    forum.toThreadForum(),
                    anti.toThreadAnti(),
                    page.current_page,
                    page.new_total_page,
                    page.has_more != 0,
                    threadDetail.nextPagePostId(
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
                ) as ThreadPartialChange
            }
            .onStart { emit(ThreadPartialChange.Load.Start) }
            .catch { emit(ThreadPartialChange.Load.Failure(it)) }
}
