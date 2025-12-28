package com.huanchengfly.tieba.post.ui.page.thread
import com.huanchengfly.tieba.post.models.mappers.toThreadAnti
import com.huanchengfly.tieba.post.models.mappers.toThreadDetail
import com.huanchengfly.tieba.post.models.mappers.toThreadPost
import com.huanchengfly.tieba.post.models.mappers.toThreadUser
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
                    val threadDetail = thread.toThreadDetail()
                    val threadAuthorId = threadDetail.author?.id
                    val posts = postList.map { it.toThreadPost() }
                    ThreadPartialChange.LoadLatestPosts.Success(
                        author = author.toThreadUser(),
                        data = ThreadPostMapper.mapPosts(posts, threadAuthorId),
                        threadDetail = threadDetail,
                        currentPage = page.current_page,
                        totalPage = page.new_total_page,
                        hasMore = page.has_more != 0,
                        nextPagePostId = threadDetail.nextPagePostId(
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
            .map { response ->
                val data = requireNotNull(response.data_) { "pbPage data is null" }
                val page = requireNotNull(data.page) { "pbPage page is null" }
                val anti = requireNotNull(data.anti) { "pbPage anti is null" }
                val thread = requireNotNull(data.thread) { "pbPage thread is null" }
                requireNotNull(thread.author) { "pbPage author is null" }
                requireNotNull(data.forum) { "pbPage forum is null" }
                val postList = data.post_list
                val firstLatestPost = postList.firstOrNull()
                val threadAuthorId = thread.author?.id
                val posts = postList.map { it.toThreadPost() }
                ThreadPartialChange.LoadMyLatestReply.Success(
                    anti = anti.toThreadAnti(),
                    posts = ThreadPostMapper.mapPosts(posts, threadAuthorId),
                    page = page.current_page,
                    isContinuous = firstLatestPost?.floor == intent.curLatestPostFloor + 1,
                    isDesc = intent.isDesc,
                    hasNewPost = postList.any { !intent.curPostIds.contains(it.id) },
                    newPostIds = postList.map { it.id }.toImmutableList(),
                ) as ThreadPartialChange
            }
            .onStart { emit(ThreadPartialChange.LoadMyLatestReply.Start) }
            .catch { emit(ThreadPartialChange.LoadMyLatestReply.Failure(it)) }
}
