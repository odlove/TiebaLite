package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.contentRenders
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.renders
import com.huanchengfly.tieba.post.api.models.protos.subPosts
import com.huanchengfly.tieba.post.api.models.protos.updateAgreeStatus
import com.huanchengfly.tieba.post.api.models.protos.updateCollectStatus
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.removeAt
import com.huanchengfly.tieba.post.repository.ContentModerationRepository
import com.huanchengfly.tieba.post.repository.EmptyDataException
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.ThreadOperationRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.store.MergeStrategy
import com.huanchengfly.tieba.post.store.ThreadStore
import com.huanchengfly.tieba.post.models.mappers.PostMapper
import com.huanchengfly.tieba.post.models.mappers.ThreadMapper
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

private fun ThreadInfo.getNextPagePostId(
    postIds: List<Long> = emptyList(),
    sortType: Int = ThreadSortType.SORT_TYPE_DEFAULT
): Long {
    val fetchedPostIds = pids.split(",")
        .filterNot { it.isBlank() }
        .map { it.toLong() }
    if (sortType == ThreadSortType.SORT_TYPE_DESC) {
        return fetchedPostIds.firstOrNull() ?: 0
    }
    val nextPostIds = fetchedPostIds.filterNot { pid -> postIds.contains(pid) }
    return if (nextPostIds.isNotEmpty()) nextPostIds.last() else 0
}

@Stable
@HiltViewModel
class ThreadViewModel @Inject constructor(
    val pbPageRepository: PbPageRepository,  // ✅ 公开，供 UI 订阅 Repository StateFlow
    private val userInteractionRepository: UserInteractionRepository,
    private val threadOperationRepository: ThreadOperationRepository,
    private val contentModerationRepository: ContentModerationRepository,
    val threadStore: ThreadStore,  // ✅ 公开，供 UI 订阅（待移除）
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<ThreadUiIntent, ThreadPartialChange, ThreadUiState, ThreadUiEvent>(dispatcherProvider) {
    override fun createInitialState(): ThreadUiState = ThreadUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ThreadUiIntent, ThreadPartialChange, ThreadUiState> =
        ThreadPartialChangeProducer()

    override fun dispatchEvent(partialChange: ThreadPartialChange): UiEvent? {
        return when (partialChange) {
            is ThreadPartialChange.Init.Success -> if (partialChange.postId != 0L) ThreadUiEvent.ScrollToFirstReply else null
            ThreadPartialChange.LoadPrevious.Start -> ThreadUiEvent.ScrollToFirstReply
            is ThreadPartialChange.AddFavorite.Success -> ThreadUiEvent.AddFavoriteSuccess(
                partialChange.floor
            )

            ThreadPartialChange.RemoveFavorite.Success -> ThreadUiEvent.RemoveFavoriteSuccess
            is ThreadPartialChange.Load.Success -> ThreadUiEvent.LoadSuccess(partialChange.currentPage)

            is ThreadPartialChange.LoadMyLatestReply.Success -> ThreadUiEvent.ScrollToLatestReply.takeIf {
                partialChange.hasNewPost
            }

            is ThreadPartialChange.DeletePost.Success -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_delete_success)
            )

            is ThreadPartialChange.DeletePost.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_delete_failure, partialChange.errorMessage)
            )

            is ThreadPartialChange.DeleteThread.Success -> CommonUiEvent.NavigateUp
            is ThreadPartialChange.DeleteThread.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_delete_failure, partialChange.errorMessage)
            )

            is ThreadPartialChange.UpdateFavoriteMark.Success -> ThreadUiEvent.UpdateFavoriteMarkSuccess
            is ThreadPartialChange.UpdateFavoriteMark.Failure -> ThreadUiEvent.UpdateFavoriteMarkFailure(
                partialChange.errorMessage
            )

            else -> null
        }
    }

    fun checkReportPost(postId: String) = contentModerationRepository.checkReportPost(postId)

    private inner class ThreadPartialChangeProducer :
        PartialChangeProducer<ThreadUiIntent, ThreadPartialChange, ThreadUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ThreadUiIntent>): Flow<ThreadPartialChange> =
            merge(
                intentFlow.filterIsInstance<ThreadUiIntent.Init>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.Load>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadMore>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadFirstPage>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadPrevious>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadLatestPosts>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadMyLatestReply>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.ToggleImmersiveMode>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.AddFavorite>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.RemoveFavorite>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.AgreeThread>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.AgreePost>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.DeletePost>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.DeleteThread>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.UpdateFavoriteMark>()
                    .flatMapConcat { it.producePartialChange() },
            )

        fun ThreadUiIntent.Init.producePartialChange(): Flow<ThreadPartialChange.Init> =
            flowOf<ThreadPartialChange.Init>(
                ThreadPartialChange.Init.Success(
                    threadInfo?.title.orEmpty(),
                    threadInfo?.author,
                    threadInfo,
                    threadInfo?.firstPostContent?.renders ?: emptyList(),
                    postId,
                    seeLz,
                    sortType,
                    sanitizedMeta = threadInfo?.let { ThreadMapper.fromProto(it).meta },
                )
            )
                .catch { emit(ThreadPartialChange.Init.Failure(it)) }

        fun ThreadUiIntent.Load.producePartialChange(): Flow<ThreadPartialChange.Load> =
            pbPageRepository.pbPage(
                threadId, page, postId, forumId, seeLz, sortType,
                from = from.takeIf { it == ThreadPageFrom.FROM_STORE }.orEmpty()
            )
                .map<PbPageResponse, ThreadPartialChange.Load> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val postList = response.data_.post_list
                    val firstPost = response.data_.first_floor_post
                    val notFirstPosts = postList.filterNot { it.floor == 1 }
                    val allPosts = listOfNotNull(firstPost) + notFirstPosts
                    ThreadPartialChange.Load.Success(
                        response.data_.thread.title,
                        response.data_.thread.author,
                        response.data_.user ?: User(),
                        firstPost,
                        notFirstPosts.map { PostItemData(it.wrapImmutable()) },
                        response.data_.thread,
                        response.data_.forum,
                        response.data_.anti,
                        response.data_.page.current_page,
                        response.data_.page.new_total_page,
                        response.data_.page.has_more != 0,
                        response.data_.thread.getNextPagePostId(
                            postList.map { it.id },
                            sortType
                        ),
                        response.data_.page.has_prev != 0,
                        firstPost?.contentRenders,
                        postId,
                        seeLz,
                        sortType,
                        threadId = threadId,
                        postIds = allPosts.map { it.id }.toImmutableList(),  // ✅ 新增
                    )
                }
                .onStart { emit(ThreadPartialChange.Load.Start) }
                .catch { emit(ThreadPartialChange.Load.Failure(it)) }

        fun ThreadUiIntent.LoadFirstPage.producePartialChange(): Flow<ThreadPartialChange.LoadFirstPage> =
            pbPageRepository.pbPage(threadId, 0, 0, forumId, seeLz, sortType)
                .map<PbPageResponse, ThreadPartialChange.LoadFirstPage> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val postList = response.data_.post_list
                    val firstPost = response.data_.first_floor_post
                    val notFirstPosts = postList.filterNot { it.floor == 1 }
                    val allPosts = listOfNotNull(firstPost) + notFirstPosts
                    ThreadPartialChange.LoadFirstPage.Success(
                        response.data_.thread.title,
                        response.data_.thread.author,
                        notFirstPosts.map { PostItemData(it.wrapImmutable()) },
                        response.data_.thread,
                        response.data_.page.current_page,
                        response.data_.page.new_total_page,
                        response.data_.page.has_more != 0,
                        response.data_.thread.getNextPagePostId(
                            postList.map { it.id },
                            sortType
                        ),
                        response.data_.page.has_prev != 0,
                        firstPost?.contentRenders ?: emptyList(),
                        postId = 0,
                        seeLz,
                        sortType,
                        threadId = threadId,
                        postIds = allPosts.map { it.id }.toImmutableList(),  // ✅ 新增
                    )
                }
                .onStart { emit(ThreadPartialChange.LoadFirstPage.Start) }
                .catch { emit(ThreadPartialChange.LoadFirstPage.Failure(it)) }

        fun ThreadUiIntent.LoadMore.producePartialChange(): Flow<ThreadPartialChange.LoadMore> =
            pbPageRepository.pbPage(threadId, page, postId, forumId, seeLz, sortType)
                .map<PbPageResponse, ThreadPartialChange.LoadMore> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val postList = response.data_.post_list
                    val posts = postList.filterNot { it.floor == 1 || postIds.contains(it.id) }
                    ThreadPartialChange.LoadMore.Success(
                        response.data_.thread.author,
                        posts.map { PostItemData(it.wrapImmutable()) },
                        response.data_.thread,
                        response.data_.page.current_page,
                        response.data_.page.new_total_page,
                        response.data_.page.has_more != 0,
                        response.data_.thread.getNextPagePostId(
                            postIds + posts.map { it.id },
                            sortType
                        ),
                        newPostIds = posts.map { it.id }.toImmutableList(),  // ✅ 新增：新加载的 postId 列表
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

        fun ThreadUiIntent.LoadPrevious.producePartialChange(): Flow<ThreadPartialChange.LoadPrevious> =
            pbPageRepository.pbPage(threadId, page, postId, forumId, seeLz, sortType, back = true)
                .map<PbPageResponse, ThreadPartialChange.LoadPrevious> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val postList = response.data_.post_list
                    val posts = postList.filterNot { it.floor == 1 || postIds.contains(it.id) }
                    ThreadPartialChange.LoadPrevious.Success(
                        response.data_.thread.author,
                        posts.map { PostItemData(it.wrapImmutable()) },
                        response.data_.thread,
                        response.data_.page.current_page,
                        response.data_.page.new_total_page,
                        response.data_.page.has_prev != 0,
                        newPostIds = posts.map { it.id }.toImmutableList(),  // ✅ 新增：新加载的 postId 列表
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

        fun ThreadUiIntent.LoadLatestPosts.producePartialChange(): Flow<ThreadPartialChange.LoadLatestPosts> =
            pbPageRepository.pbPage(
                threadId = threadId,
                page = 0,
                postId = curLatestPostId,
                forumId = forumId,
                seeLz = seeLz,
                sortType = sortType,
                lastPostId = curLatestPostId
            )
                .map { response ->
                    checkNotNull(response.data_)
                    checkNotNull(response.data_.thread)
                    checkNotNull(response.data_.thread.author)
                    checkNotNull(response.data_.page)
                    val postList = response.data_.post_list.filterNot { it.floor == 1 }
                    if (postList.isEmpty()) {
                        ThreadPartialChange.LoadLatestPosts.SuccessWithNoNewPost
                    } else {
                        ThreadPartialChange.LoadLatestPosts.Success(
                            author = response.data_.thread.author,
                            data = postList.map { PostItemData(it.wrapImmutable()) },
                            threadInfo = response.data_.thread,
                            currentPage = response.data_.page.current_page,
                            totalPage = response.data_.page.new_total_page,
                            hasMore = response.data_.page.has_more != 0,
                            nextPagePostId = response.data_.thread.getNextPagePostId(
                                postList.map { it.id },
                                sortType
                            ),
                            newPostIds = postList.map { it.id }.toImmutableList(),  // ✅ 新增：新加载的 postId 列表
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

        fun ThreadUiIntent.LoadMyLatestReply.producePartialChange(): Flow<ThreadPartialChange.LoadMyLatestReply> =
            pbPageRepository.pbPage(threadId, page = 0, postId = postId, forumId = forumId)
                .map<PbPageResponse, ThreadPartialChange.LoadMyLatestReply> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val firstLatestPost = response.data_.post_list.firstOrNull()
                    val postList = response.data_.post_list
                    ThreadPartialChange.LoadMyLatestReply.Success(
                        anti = response.data_.anti,
                        posts = postList.map { PostItemData(it.wrapImmutable()) },
                        page = response.data_.page.current_page,
                        isContinuous = firstLatestPost?.floor == curLatestPostFloor + 1,
                        isDesc = isDesc,
                        hasNewPost = postList.any { !curPostIds.contains(it.id) },
                        newPostIds = postList.map { it.id }.toImmutableList(),  // ✅ 新增
                    )
                }
                .onStart { emit(ThreadPartialChange.LoadMyLatestReply.Start) }
                .catch { emit(ThreadPartialChange.LoadMyLatestReply.Failure(it)) }

        fun ThreadUiIntent.ToggleImmersiveMode.producePartialChange(): Flow<ThreadPartialChange.ToggleImmersiveMode> =
            flowOf(ThreadPartialChange.ToggleImmersiveMode.Success(isImmersiveMode))

        fun ThreadUiIntent.AddFavorite.producePartialChange(): Flow<ThreadPartialChange.AddFavorite> {
            var previousCollectStatus = 0
            var previousCollectMarkPid = 0L
            return threadOperationRepository
                .addStore(threadId, postId)
                .map { response ->
                    if (response.errorCode == 0) {
                        pbPageRepository.updateThreadMeta(threadId) { meta ->
                            meta.copy(
                                collectStatus = 1,
                                collectMarkPid = postId
                            )
                        }
                        ThreadPartialChange.AddFavorite.Success(
                            postId, floor
                        )
                    } else {
                        pbPageRepository.updateThreadMeta(threadId) { meta ->
                            meta.copy(
                                collectStatus = previousCollectStatus,
                                collectMarkPid = previousCollectMarkPid
                            )
                        }
                        ThreadPartialChange.AddFavorite.Failure(
                            response.errorCode,
                            response.errorMsg
                        )
                    }
                }
                .onStart {
                    pbPageRepository.updateThreadMeta(threadId) { meta ->
                        previousCollectStatus = meta.collectStatus
                        previousCollectMarkPid = meta.collectMarkPid
                        meta.copy(
                            collectStatus = 1,
                            collectMarkPid = postId
                        )
                    }
                    emit(ThreadPartialChange.AddFavorite.Start)
                }
                .catch {
                    pbPageRepository.updateThreadMeta(threadId) { meta ->
                        meta.copy(
                            collectStatus = previousCollectStatus,
                            collectMarkPid = previousCollectMarkPid
                        )
                    }
                    emit(
                        ThreadPartialChange.AddFavorite.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }
        }

        fun ThreadUiIntent.RemoveFavorite.producePartialChange(): Flow<ThreadPartialChange.RemoveFavorite> {
            var previousCollectStatus = 0
            var previousCollectMarkPid = 0L
            return threadOperationRepository
                .removeStore(threadId, forumId, tbs)
                .map { response ->
                    if (response.errorCode == 0) {
                        pbPageRepository.updateThreadMeta(threadId) { meta ->
                            meta.copy(
                                collectStatus = 0,
                                collectMarkPid = 0
                            )
                        }
                        ThreadPartialChange.RemoveFavorite.Success
                    } else {
                        pbPageRepository.updateThreadMeta(threadId) { meta ->
                            meta.copy(
                                collectStatus = previousCollectStatus,
                                collectMarkPid = previousCollectMarkPid
                            )
                        }
                        ThreadPartialChange.RemoveFavorite.Failure(
                            response.errorCode,
                            response.errorMsg
                        )
                    }
                }
                .onStart {
                    pbPageRepository.updateThreadMeta(threadId) { meta ->
                        previousCollectStatus = meta.collectStatus
                        previousCollectMarkPid = meta.collectMarkPid
                        meta.copy(
                            collectStatus = 0,
                            collectMarkPid = 0
                        )
                    }
                    emit(ThreadPartialChange.RemoveFavorite.Start)
                }
                .catch {
                    pbPageRepository.updateThreadMeta(threadId) { meta ->
                        meta.copy(
                            collectStatus = previousCollectStatus,
                            collectMarkPid = previousCollectMarkPid
                        )
                    }
                    emit(
                        ThreadPartialChange.RemoveFavorite.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }
        }

        fun ThreadUiIntent.AgreeThread.producePartialChange(): Flow<ThreadPartialChange.AgreeThread> {
            var previousHasAgree = 0
            var previousAgreeNum = 0

            return userInteractionRepository
                .opAgree(
                    threadId.toString(),
                    postId.toString(),
                    hasAgree = if (agree) 0 else 1,
                    objType = 3
                )
                .map<AgreeBean, ThreadPartialChange.AgreeThread> {
                    ThreadPartialChange.AgreeThread.Success(agree)
                }
                .catch {
                    // ✅ 失败时恢复原始值：调用 PbPageRepository 回滚缓存
                    pbPageRepository.updateThreadMeta(threadId) { meta: com.huanchengfly.tieba.post.models.ThreadMeta ->
                        meta.copy(
                            hasAgree = previousHasAgree,
                            agreeNum = previousAgreeNum
                        )
                    }
                    emit(
                        ThreadPartialChange.AgreeThread.Failure(
                            !agree,
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }
                .onStart {
                    // ✅ 从 PbPageRepository 缓存获取原始值 + 乐观更新
                    val currentEntity = pbPageRepository.threadFlow(threadId).value
                    if (currentEntity != null) {
                        previousHasAgree = currentEntity.meta.hasAgree
                        previousAgreeNum = currentEntity.meta.agreeNum

                        // 乐观更新：立即更新缓存，触发 UI 刷新
                        pbPageRepository.updateThreadMeta(threadId) { meta: com.huanchengfly.tieba.post.models.ThreadMeta ->
                            meta.copy(
                                hasAgree = if (agree) 1 else 0,
                                agreeNum = if (agree) meta.agreeNum + 1 else meta.agreeNum - 1
                            )
                        }
                    }
                    emit(ThreadPartialChange.AgreeThread.Start(agree))
                }
        }

        fun ThreadUiIntent.AgreePost.producePartialChange(): Flow<ThreadPartialChange.AgreePost> {
            var previousHasAgree = 0
            var previousAgreeNum = 0

            return userInteractionRepository
                .opAgree(
                    threadId.toString(),
                    postId.toString(),
                    hasAgree = if (agree) 0 else 1,
                    objType = 1
                )
                .map<AgreeBean, ThreadPartialChange.AgreePost> {
                    ThreadPartialChange.AgreePost.Success(postId, agree)
                }
                .catch {
                    // ✅ 失败时恢复原始值：调用 PbPageRepository 回滚缓存
                    pbPageRepository.updatePostMeta(threadId, postId) { meta: com.huanchengfly.tieba.post.models.PostMeta ->
                        meta.copy(
                            hasAgree = previousHasAgree,
                            agreeNum = previousAgreeNum
                        )
                    }
                    emit(
                        ThreadPartialChange.AgreePost.Failure(
                            postId,
                            !agree,
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }
                .onStart {
                    // ✅ 从 PbPageRepository 缓存获取原始值 + 乐观更新
                    val currentPost = pbPageRepository.postFlow(threadId, postId).value
                    if (currentPost != null) {
                        previousHasAgree = currentPost.meta.hasAgree
                        previousAgreeNum = currentPost.meta.agreeNum

                        // 乐观更新：立即更新缓存，触发 UI 刷新
                        pbPageRepository.updatePostMeta(threadId, postId) { meta: com.huanchengfly.tieba.post.models.PostMeta ->
                            meta.copy(
                                hasAgree = if (agree) 1 else 0,
                                agreeNum = if (agree) meta.agreeNum + 1 else meta.agreeNum - 1
                            )
                        }
                    }
                    emit(ThreadPartialChange.AgreePost.Start(postId, agree))
                }
        }

        fun ThreadUiIntent.DeletePost.producePartialChange(): Flow<ThreadPartialChange.DeletePost> =
            threadOperationRepository
                .delPost(forumId, forumName, threadId, postId, tbs, false, deleteMyPost)
                .map<CommonResponse, ThreadPartialChange.DeletePost> {
                    ThreadPartialChange.DeletePost.Success(postId)
                }
                .catch {
                    emit(
                        ThreadPartialChange.DeletePost.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.DeleteThread.producePartialChange(): Flow<ThreadPartialChange.DeleteThread> =
            threadOperationRepository
                .delThread(forumId, forumName, threadId, tbs, deleteMyThread, false)
                .map<CommonResponse, ThreadPartialChange.DeleteThread> {
                    ThreadPartialChange.DeleteThread.Success
                }
                .catch {
                    emit(
                        ThreadPartialChange.DeleteThread.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.UpdateFavoriteMark.producePartialChange(): Flow<ThreadPartialChange.UpdateFavoriteMark> {
            var previousCollectStatus = 0
            var previousCollectMarkPid = 0L
            return threadOperationRepository
                .addStore(threadId, postId)
                .map { response ->
                    if (response.errorCode == 0) {
                        pbPageRepository.updateThreadMeta(threadId) { meta ->
                            meta.copy(
                                collectStatus = 1,
                                collectMarkPid = postId
                            )
                        }
                        ThreadPartialChange.UpdateFavoriteMark.Success(postId)
                    } else {
                        pbPageRepository.updateThreadMeta(threadId) { meta ->
                            meta.copy(
                                collectStatus = previousCollectStatus,
                                collectMarkPid = previousCollectMarkPid
                            )
                        }
                        ThreadPartialChange.UpdateFavoriteMark.Failure(
                            response.errorCode,
                            response.errorMsg
                        )
                    }
                }
                .onStart {
                    pbPageRepository.updateThreadMeta(threadId) { meta ->
                        previousCollectStatus = meta.collectStatus
                        previousCollectMarkPid = meta.collectMarkPid
                        meta.copy(
                            collectStatus = 1,
                            collectMarkPid = postId
                        )
                    }
                    emit(ThreadPartialChange.UpdateFavoriteMark.Start)
                }
                .catch {
                    pbPageRepository.updateThreadMeta(threadId) { meta ->
                        meta.copy(
                            collectStatus = previousCollectStatus,
                            collectMarkPid = previousCollectMarkPid
                        )
                    }
                    emit(
                        ThreadPartialChange.UpdateFavoriteMark.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }
        }
    }
}

sealed interface ThreadUiIntent : UiIntent {
    data class Init(
        val threadId: Long,
        val forumId: Long? = null,
        val postId: Long = 0,
        val threadInfo: ThreadInfo? = null,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
    ) : ThreadUiIntent

    data class Load(
        val threadId: Long,
        val page: Int = 1,
        val postId: Long = 0,
        val forumId: Long? = null,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
        val from: String = ""
    ) : ThreadUiIntent

    data class LoadFirstPage(
        val threadId: Long,
        val forumId: Long? = null,
        val seeLz: Boolean = false,
        val sortType: Int = 0
    ) : ThreadUiIntent

    data class LoadMore(
        val threadId: Long,
        val page: Int,
        val forumId: Long? = null,
        val postId: Long = 0,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
        val postIds: List<Long> = emptyList(),
    ) : ThreadUiIntent

    data class LoadPrevious(
        val threadId: Long,
        val page: Int,
        val forumId: Long? = null,
        val postId: Long = 0,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
        val postIds: List<Long> = emptyList(),
    ) : ThreadUiIntent

    /**
     * 加载当前贴子的最新回复
     */
    data class LoadLatestPosts(
        val threadId: Long,
        val curLatestPostId: Long,
        val forumId: Long? = null,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
    ) : ThreadUiIntent

    /**
     * 当前用户发送新的回复时，加载用户发送的回复
     */
    data class LoadMyLatestReply(
        val threadId: Long,
        val postId: Long,
        val forumId: Long? = null,
        val isDesc: Boolean = false,
        val curLatestPostFloor: Int = 0,
        val curPostIds: List<Long> = emptyList(),
    ) : ThreadUiIntent

    data class ToggleImmersiveMode(
        val isImmersiveMode: Boolean,
    ) : ThreadUiIntent

    data class AddFavorite(
        val threadId: Long,
        val postId: Long,
        val floor: Int
    ) : ThreadUiIntent

    data class RemoveFavorite(
        val threadId: Long,
        val forumId: Long,
        val tbs: String?
    ) : ThreadUiIntent

    data class AgreeThread(
        val threadId: Long,
        val postId: Long,
        val agree: Boolean
    ) : ThreadUiIntent

    data class AgreePost(
        val threadId: Long,
        val postId: Long,
        val agree: Boolean
    ) : ThreadUiIntent

    data class DeletePost(
        val forumId: Long,
        val forumName: String,
        val threadId: Long,
        val postId: Long,
        val deleteMyPost: Boolean,
        val tbs: String? = null
    ) : ThreadUiIntent

    data class DeleteThread(
        val forumId: Long,
        val forumName: String,
        val threadId: Long,
        val deleteMyThread: Boolean,
        val tbs: String? = null
    ) : ThreadUiIntent

    data class UpdateFavoriteMark(
        val threadId: Long,
        val postId: Long
    ) : ThreadUiIntent
}

sealed interface ThreadPartialChange : PartialChange<ThreadUiState> {
    sealed class Init : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> oldState.copy(
                isRefreshing = true,
                isError = false,
                error = null,
                title = title,
                author = if (author != null) wrapImmutable(author) else null,
                threadInfo = threadInfo?.wrapImmutable(),
                firstPost = if (threadInfo != null && author != null)
                    wrapImmutable(
                        Post(
                            title = title,
                            author = author,
                            floor = 1,
                            time = threadInfo.createTime
                        )
                    ) else null,
                firstPostContentRenders = firstPostContentRenders.toImmutableList(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                initMeta = sanitizedMeta,
            )

            is Failure -> oldState.copy(
                isError = true,
                error = error.wrapImmutable()
            )
        }

        data class Success(
            val title: String,
            val author: User?,
            val threadInfo: ThreadInfo?,
            val firstPostContentRenders: List<PbContentRender>,
            val postId: Long = 0,
            val seeLz: Boolean = false,
            val sortType: Int = 0,
            val sanitizedMeta: com.huanchengfly.tieba.post.models.ThreadMeta? = null,
        ) : Init()

        data class Failure(
            val error: Throwable
        ) : Init()
    }

    sealed class Load : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)

            is Success -> oldState.copy(
                isRefreshing = false,
                isError = false,
                error = null,
                title = title,
                author = wrapImmutable(author),
                user = wrapImmutable(user),
                data = data.toImmutableList(),
                threadInfo = threadInfo.wrapImmutable(),
                firstPost = if (firstPost != null) wrapImmutable(firstPost) else oldState.firstPost,
                forum = wrapImmutable(forum),
                anti = wrapImmutable(anti),
                currentPageMin = currentPage,
                currentPageMax = currentPage,
                totalPage = totalPage,
                hasMore = hasMore,
                nextPagePostId = nextPagePostId,
                hasPrevious = hasPrevious,
                firstPostContentRenders = firstPostContentRenders?.toImmutableList()
                    ?: oldState.firstPostContentRenders,
                latestPosts = persistentListOf(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                threadId = threadId,  // ✅ 新增
                postIds = postIds,  // ✅ 新增
            )

            is Failure -> oldState.copy(
                isRefreshing = false,
                isError = true,
                error = error.wrapImmutable()
            )
        }

        data object Start : Load()

        data class Success(
            val title: String,
            val author: User,
            val user: User,
            val firstPost: Post?,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val forum: SimpleForum,
            val anti: Anti,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val hasPrevious: Boolean,
            val firstPostContentRenders: List<PbContentRender>?,
            val postId: Long = 0,
            val seeLz: Boolean = false,
            val sortType: Int = 0,
            val threadId: Long,  // ✅ 新增
            val postIds: ImmutableList<Long>,  // ✅ 新增
        ) : Load()

        data class Failure(
            val error: Throwable,
        ) : Load()
    }

    sealed class LoadFirstPage : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                isRefreshing = false,
                isError = false,
                error = null,
                title = title,
                author = wrapImmutable(author),
                data = data.toImmutableList(),
                threadInfo = threadInfo.wrapImmutable(),
                currentPageMin = currentPage,
                currentPageMax = currentPage,
                totalPage = totalPage,
                hasMore = hasMore,
                nextPagePostId = nextPagePostId,
                hasPrevious = hasPrevious,
                firstPostContentRenders = firstPostContentRenders.toImmutableList(),
                latestPosts = persistentListOf(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                threadId = threadId,  // ✅ 新增
                postIds = postIds,  // ✅ 新增
            )

            is Failure -> oldState.copy(
                isRefreshing = false,
                isError = true,
                error = error.wrapImmutable(),
            )
        }

        data object Start : LoadFirstPage()

        data class Success(
            val title: String,
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val hasPrevious: Boolean,
            val firstPostContentRenders: List<PbContentRender>,
            val postId: Long,
            val seeLz: Boolean,
            val sortType: Int,
            val threadId: Long,  // ✅ 新增
            val postIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadFirstPage()

        data class Failure(
            val error: Throwable
        ) : LoadFirstPage()
    }

    sealed class LoadMore : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isLoadingMore = true)
            is Success -> {
                val uniqueData = data.filterNot { item ->
                    oldState.data.any { it.post.get { id } == item.post.get { id } }
                }
                oldState.copy(
                    isLoadingMore = false,
                    author = wrapImmutable(author),
                    data = (oldState.data + uniqueData).toImmutableList(),
                    threadInfo = threadInfo.wrapImmutable(),
                    currentPageMax = currentPage,
                    totalPage = totalPage,
                    hasMore = hasMore,
                    nextPagePostId = nextPagePostId,
                    latestPosts = persistentListOf(),
                    postIds = (oldState.postIds + newPostIds).distinct().toImmutableList(),  // ✅ 新增
                )
            }

            is Failure -> oldState.copy(isLoadingMore = false)
        }

        data object Start : LoadMore()

        data class Success(
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val newPostIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadMore()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : LoadMore()
    }

    sealed class LoadPrevious : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                isRefreshing = false,
                author = wrapImmutable(author),
                data = (data + oldState.data).toImmutableList(),
                threadInfo = threadInfo.wrapImmutable(),
                currentPageMin = currentPage,
                totalPage = totalPage,
                hasPrevious = hasPrevious,
                postIds = (newPostIds + oldState.postIds).distinct().toImmutableList(),  // ✅ 新增
            )

            is Failure -> oldState.copy(isRefreshing = false)
        }

        data object Start : LoadPrevious()

        data class Success(
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasPrevious: Boolean,
            val newPostIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadPrevious()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String,
        ) : LoadPrevious()
    }

    sealed class LoadLatestPosts : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            Start -> oldState.copy(isLoadingMore = true)
            is Success -> {
                val uniqueData = data.filterNot { item ->
                    oldState.data.any { it.post.get { id } == item.post.get { id } }
                }
                oldState.copy(
                    isLoadingMore = false,
                    author = wrapImmutable(author),
                    data = (oldState.data + uniqueData).toImmutableList(),
                    threadInfo = threadInfo.wrapImmutable(),
                    currentPageMax = currentPage,
                    totalPage = totalPage,
                    hasMore = hasMore,
                    nextPagePostId = nextPagePostId,
                    latestPosts = persistentListOf(),
                    postIds = (oldState.postIds + newPostIds).distinct().toImmutableList(),  // ✅ 新增
                )
            }

            SuccessWithNoNewPost -> oldState.copy(isLoadingMore = false)
            is Failure -> oldState.copy(isLoadingMore = false)
        }

        data object Start : LoadLatestPosts()

        data class Success(
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val newPostIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadLatestPosts()

        data object SuccessWithNoNewPost : LoadLatestPosts()

        data class Failure(
            val error: Throwable,
        ) : LoadLatestPosts()
    }

    sealed class LoadMyLatestReply : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState =
            when (this) {
                Start -> oldState.copy(isLoadingLatestReply = true)
                is Success -> {
                    val continuous = isContinuous || page == oldState.currentPageMax
                    val replacePostIndexes = oldState.data.mapIndexedNotNull { index, item ->
                        val replaceItemIndex =
                            posts.indexOfFirst { it.post.get { id } == item.post.get { id } }
                        if (replaceItemIndex != -1) index to replaceItemIndex else null
                    }
                    val newPost = oldState.data.mapIndexed { index, oldItem ->
                        val replaceIndex = replacePostIndexes.firstOrNull { it.first == index }
                        if (replaceIndex != null) posts[replaceIndex.second] else oldItem
                    }
                    val addPosts = posts.filter {
                        !newPost.any { item -> item.post.get { id } == it.post.get { id } }
                    }
                    when {
                        hasNewPost && continuous && isDesc -> {
                            val newData = (addPosts.reversed() + newPost).toImmutableList()
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newData,
                                latestPosts = persistentListOf(),
                                postIds = newData.map { it.post.get { id } }.distinct().toImmutableList(),  // ✅ 新增
                            )
                        }

                        hasNewPost && continuous && !isDesc -> {
                            val newData = (newPost + addPosts).toImmutableList()
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newData,
                                latestPosts = persistentListOf(),
                                postIds = newData.map { it.post.get { id } }.distinct().toImmutableList(),  // ✅ 新增
                            )
                        }

                        hasNewPost -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newPost.toImmutableList(),
                                latestPosts = posts.toImmutableList(),
                                postIds = newPost.map { it.post.get { id } }.distinct().toImmutableList(),  // ✅ 新增
                            )
                        }

                        !hasNewPost -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newPost.toImmutableList(),
                                latestPosts = persistentListOf(),
                                postIds = newPost.map { it.post.get { id } }.distinct().toImmutableList(),  // ✅ 新增
                            )
                        }

                        else -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                            )
                        }
                    }
                }

                is Failure -> oldState.copy(
                    isLoadingLatestReply = false,
                    isError = true,
                    error = error.wrapImmutable(),
                )
            }

        object Start : LoadMyLatestReply()

        data class Success(
            val anti: Anti,
            val posts: List<PostItemData>,
            val page: Int,
            val isContinuous: Boolean,
            val isDesc: Boolean,
            val hasNewPost: Boolean,
            val newPostIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadMyLatestReply()

        data class Failure(
            val error: Throwable,
        ) : LoadMyLatestReply()
    }

    sealed class ToggleImmersiveMode : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> oldState.copy(isImmersiveMode = isImmersiveMode)
        }

        data class Success(
            val isImmersiveMode: Boolean
        ) : ToggleImmersiveMode()
    }

    sealed class AddFavorite : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                Start -> oldState
                is Success -> oldState.copy(
                    threadInfo = oldState.threadInfo?.getImmutable {
                        updateCollectStatus(
                            newStatus = 1,
                            markPostId = markPostId
                        )
                    }
                )

                is Failure -> oldState
            }
        }

        object Start : AddFavorite()

        data class Success(
            val markPostId: Long,
            val floor: Int
        ) : AddFavorite()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : AddFavorite()
    }

    sealed class RemoveFavorite : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                Start -> oldState
                Success -> oldState.copy(
                    threadInfo = oldState.threadInfo?.getImmutable {
                        updateCollectStatus(
                            newStatus = 0,
                            markPostId = 0
                        )
                    }
                )

                is Failure -> oldState
            }
        }

        object Start : RemoveFavorite()

        object Success : RemoveFavorite()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : RemoveFavorite()
    }

    sealed class AgreeThread : ThreadPartialChange {
        // ✅ 删除 Proto 更新逻辑，Store 已处理更新
        override fun reduce(oldState: ThreadUiState): ThreadUiState =
            when (this) {
                is Start, is Success, is Failure -> oldState  // ✅ Store 已更新，State 无需变化
            }

        data class Start(
            val hasAgree: Boolean
        ) : AgreeThread()

        data class Success(
            val hasAgree: Boolean
        ) : AgreeThread()

        data class Failure(
            val hasAgree: Boolean,
            val errorCode: Int,
            val errorMessage: String
        ) : AgreeThread()
    }

    sealed class AgreePost : ThreadPartialChange {
        // ✅ 删除 Proto 更新逻辑，Store 已处理更新
        override fun reduce(oldState: ThreadUiState): ThreadUiState =
            when (this) {
                is Start, is Success, is Failure -> oldState  // ✅ Store 已更新，State 无需变化
            }

        data class Start(
            val postId: Long,
            val hasAgree: Boolean
        ) : AgreePost()

        data class Success(
            val postId: Long,
            val hasAgree: Boolean
        ) : AgreePost()

        data class Failure(
            val postId: Long,
            val hasAgree: Boolean,
            val errorCode: Int,
            val errorMessage: String
        ) : AgreePost()
    }

    sealed class DeletePost : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> {
                val deletedPostIndex = oldState.data.indexOfFirst { it.post.get { id } == postId }
                oldState.copy(
                    data = oldState.data.removeAt(deletedPostIndex),
                )
            }

            is Failure -> oldState
        }

        data class Success(
            val postId: Long
        ) : DeletePost()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : DeletePost()
    }

    sealed class DeleteThread : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = oldState

        object Success : DeleteThread()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : DeleteThread()
    }

    sealed class UpdateFavoriteMark : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                Start -> oldState
                is Success -> oldState.copy(
                    threadInfo = oldState.threadInfo?.getImmutable {
                        updateCollectStatus(
                            newStatus = 1,
                            markPostId = markPostId
                        )
                    }
                )

                is Failure -> oldState
            }
        }

        object Start : UpdateFavoriteMark()

        data class Success(
            val markPostId: Long
        ) : UpdateFavoriteMark()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : UpdateFavoriteMark()
    }
}

data class ThreadUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingLatestReply: Boolean = false,
    val isError: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,

    val hasMore: Boolean = true,
    val nextPagePostId: Long = 0,
    val hasPrevious: Boolean = false,
    val currentPageMin: Int = 0,
    val currentPageMax: Int = 0,
    val totalPage: Int = 0,

    val seeLz: Boolean = false,
    val sortType: Int = ThreadSortType.SORT_TYPE_DEFAULT,
    val postId: Long = 0,
    val threadId: Long = 0,  // ✅ 新增：Store 订阅用的 threadId

    val title: String = "",
    val author: ImmutableHolder<User>? = null,
    val user: ImmutableHolder<User> = wrapImmutable(User()),
    val threadInfo: ImmutableHolder<ThreadInfo>? = null,
    val firstPost: ImmutableHolder<Post>? = null,
    val forum: ImmutableHolder<SimpleForum>? = null,
    val anti: ImmutableHolder<Anti>? = null,

    val firstPostContentRenders: ImmutableList<PbContentRender> = persistentListOf(),
    val data: ImmutableList<PostItemData> = persistentListOf(),
    val latestPosts: ImmutableList<PostItemData> = persistentListOf(),
    val postIds: ImmutableList<Long> = persistentListOf(),  // ✅ 新增：Store 订阅用的 postId 列表

    val initMeta: com.huanchengfly.tieba.post.models.ThreadMeta? = null,
    val isImmersiveMode: Boolean = false,
) : UiState

sealed interface ThreadUiEvent : UiEvent {
    data object ScrollToFirstReply : ThreadUiEvent

    data object ScrollToLatestReply : ThreadUiEvent

    data class LoadSuccess(
        val page: Int
    ) : ThreadUiEvent

    data class AddFavoriteSuccess(val floor: Int) : ThreadUiEvent

    data object RemoveFavoriteSuccess : ThreadUiEvent

    data object UpdateFavoriteMarkSuccess : ThreadUiEvent

    data class UpdateFavoriteMarkFailure(
        val errorMessage: String
    ) : ThreadUiEvent
}

object ThreadSortType {
    const val SORT_TYPE_ASC = 0
    const val SORT_TYPE_DESC = 1
    const val SORT_TYPE_HOT = 2
    const val SORT_TYPE_DEFAULT = SORT_TYPE_ASC
}

@Immutable
data class PostItemData(
    val post: ImmutableHolder<Post>,
    val blocked: Boolean = post.get { shouldBlock() },
    val contentRenders: ImmutableList<PbContentRender> = post.get { this.contentRenders },
    val subPosts: ImmutableList<SubPostItemData> = post.get { this.subPosts },
)

@Immutable
data class SubPostItemData(
    val subPost: ImmutableHolder<SubPostList>,
    val subPostContent: AnnotatedString,
    val blocked: Boolean = subPost.get { shouldBlock() },
) {
    val id: Long
        get() = subPost.get { id }

    val author: ImmutableHolder<User>?
        get() = subPost.get { author }?.wrapImmutable()
}
