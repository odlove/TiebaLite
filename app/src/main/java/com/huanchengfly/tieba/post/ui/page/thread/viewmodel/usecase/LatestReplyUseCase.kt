package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.repository.EmptyDataException
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
class LoadLatestPostsUseCase @Inject constructor(
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.LoadLatestPosts> {
    override fun execute(intent: ThreadUiIntent.LoadLatestPosts): Flow<ThreadPartialChange> =
        pbPageRepository.pbPage(
            threadId = intent.threadId,
            page = 0,
            postId = intent.curLatestPostId,
            forumId = intent.forumId,
            seeLz = intent.seeLz,
            sortType = intent.sortType,
            lastPostId = intent.curLatestPostId
        )
            .map { response ->
                val data = checkNotNull(response.data_)
                val thread = checkNotNull(data.thread)
                val author = checkNotNull(thread.author)
                val page = checkNotNull(data.page)
                val postList = data.post_list.filterNot { it.floor == 1 }
                if (postList.isEmpty()) {
                    ThreadPartialChange.LoadLatestPosts.SuccessWithNoNewPost
                } else {
                    ThreadPartialChange.LoadLatestPosts.Success(
                        author = author,
                        data = ThreadPostMapper.mapPosts(postList),
                        threadInfo = thread,
                        currentPage = page.current_page,
                        totalPage = page.new_total_page,
                        hasMore = page.has_more != 0,
                        nextPagePostId = thread.nextPagePostId(
                            postList.map { it.id },
                            intent.sortType
                        ),
                        newPostIds = postList.map { it.id }.toImmutableList(),
                    )
                }
            }
            .onStart { emit(ThreadPartialChange.LoadLatestPosts.Start) }
            .catch {
                if (it is EmptyDataException) {
                    emit(ThreadPartialChange.LoadLatestPosts.SuccessWithNoNewPost)
                } else {
                    emit(ThreadPartialChange.LoadLatestPosts.Failure(it))
                }
            }
}

@ViewModelScoped
class LoadMyLatestReplyUseCase @Inject constructor(
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.LoadMyLatestReply> {
    override fun execute(intent: ThreadUiIntent.LoadMyLatestReply): Flow<ThreadPartialChange> =
        pbPageRepository.pbPage(intent.threadId, page = 0, postId = intent.postId, forumId = intent.forumId)
            .map<PbPageResponse, ThreadPartialChange> { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val page = data.page ?: throw TiebaUnknownException
                val anti = data.anti ?: throw TiebaUnknownException
                val thread = data.thread ?: throw TiebaUnknownException
                thread.author ?: throw TiebaUnknownException
                data.forum ?: throw TiebaUnknownException
                val postList = data.post_list
                val firstLatestPost = postList.firstOrNull()
                ThreadPartialChange.LoadMyLatestReply.Success(
                    anti = anti,
                    posts = ThreadPostMapper.mapPosts(postList),
                    page = page.current_page,
                    isContinuous = firstLatestPost?.floor == intent.curLatestPostFloor + 1,
                    isDesc = intent.isDesc,
                    hasNewPost = postList.any { !intent.curPostIds.contains(it.id) },
                    newPostIds = postList.map { it.id }.toImmutableList(),
                )
            }
            .onStart { emit(ThreadPartialChange.LoadMyLatestReply.Start) }
            .catch { emit(ThreadPartialChange.LoadMyLatestReply.Failure(it)) }
}
