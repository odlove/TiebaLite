package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.huanchengfly.tieba.core.common.history.HistoryItem
import com.huanchengfly.tieba.core.common.history.HistoryRepository
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.common.thread.ThreadPost
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val HISTORY_SAVE_DEBOUNCE_MS = 750L

@Stable
data class ThreadFeeds(
    val effectiveThreadId: Long,
    val threadMeta: ThreadMeta?,
    val postEntities: List<PostEntity>,
    val postIds: List<Long>
)

@Composable
fun rememberThreadFeeds(
    viewModel: ThreadViewModel,
    routeThreadId: Long,
    uiState: ThreadUiState
): ThreadFeeds {
    val effectiveThreadId = uiState.threadId.takeIf { it != 0L } ?: routeThreadId
    val threadMetaFlow = remember(viewModel, effectiveThreadId) {
        viewModel.threadMetaStore.metaFlow(effectiveThreadId)
    }
    val threadMeta by threadMetaFlow.collectAsState(initial = null)
    val postIds = remember(uiState.postIds) { uiState.postIds.toList() }
    val postsFlow = remember(viewModel, effectiveThreadId, postIds) {
        viewModel.pbPageRepository.postsFlow(
            effectiveThreadId,
            postIds
        )
    }
    val postEntities by postsFlow.collectAsState(initial = emptyList())

    return remember(effectiveThreadId, threadMeta, postEntities, postIds) {
        ThreadFeeds(
            effectiveThreadId = effectiveThreadId,
            threadMeta = threadMeta,
            postEntities = postEntities,
            postIds = postIds
        )
    }
}

@Composable
fun rememberThreadHistorySaver(
    historyRepository: HistoryRepository,
    routeThreadId: Long,
    pageState: ThreadPageState,
    lastVisibilityPostId: Long,
    lastVisibilityPost: ImmutableHolder<ThreadPost>?
) {
    var savedHistory by remember(routeThreadId) { mutableStateOf(false) }
    var lastSavedPostId by remember(routeThreadId) { mutableStateOf(0L) }
    val effectiveThreadId = pageState.threadId.takeIf { it != 0L } ?: routeThreadId
    LaunchedEffect(effectiveThreadId, pageState.threadTitle, pageState.author, lastVisibilityPostId) {
        if (effectiveThreadId == 0L) {
            return@LaunchedEffect
        }
        if (pageState.threadTitle.isBlank()) {
            return@LaunchedEffect
        }
        val savingWithoutVisiblePost = lastVisibilityPostId == 0L
        if (savingWithoutVisiblePost && savedHistory) {
            return@LaunchedEffect
        }
        if (!savingWithoutVisiblePost && savedHistory && lastVisibilityPostId == lastSavedPostId) {
            return@LaunchedEffect
        }

        delay(HISTORY_SAVE_DEBOUNCE_MS)

        val history = HistoryItem(
            title = pageState.threadTitle,
            data = effectiveThreadId.toString(),
            type = HistoryRepository.TYPE_THREAD,
            extras = ThreadHistoryInfoBean(
                isSeeLz = pageState.isSeeLz,
                pid = lastVisibilityPostId.takeIf { it != 0L }?.toString(),
                forumName = pageState.forum?.get { name },
                floor = lastVisibilityPost?.get { floor }?.toString()
                    ?.takeIf { lastVisibilityPostId != 0L }
            ).toJson(),
            avatar = StringUtil.getAvatarUrl(pageState.author?.get { portrait }),
            username = pageState.author?.get { nameShow }
        )

        try {
            withContext(Dispatchers.IO) {
                historyRepository.saveHistory(history, async = false)
            }
            savedHistory = true
            lastSavedPostId = lastVisibilityPostId
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
