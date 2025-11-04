package com.huanchengfly.tieba.post.runtime.preview

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.network.exception.TiebaException
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.core.runtime.preview.ClipBoardLink
import com.huanchengfly.tieba.core.runtime.preview.ForumLink
import com.huanchengfly.tieba.core.runtime.preview.QuickPreviewService
import com.huanchengfly.tieba.core.runtime.preview.SimpleLink
import com.huanchengfly.tieba.core.runtime.preview.ThreadLink
import com.huanchengfly.tieba.core.runtime.preview.Icon
import com.huanchengfly.tieba.core.runtime.preview.PreviewInfo
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.preview.QuickPreviewRepository
import com.huanchengfly.tieba.post.preview.ThreadPreviewData
import com.huanchengfly.tieba.post.preview.ForumPreviewData
import com.huanchengfly.tieba.post.ui.page.forum.getSortType
import com.huanchengfly.tieba.post.utils.StringUtil
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class AppQuickPreviewService @Inject constructor(
    private val repository: QuickPreviewRepository,
    private val resourceProvider: ResourceProvider,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) : QuickPreviewService {

    override fun getPreviewFlow(
        context: Context,
        link: ClipBoardLink,
        lifecycle: Lifecycle?
    ): Flow<PreviewInfo?> {
        val detailFlow: Flow<PreviewInfo>? = when (link) {
            is ForumLink -> repository.observeForum(
                forumName = link.forumName,
                sortType = getSortType(context, link.forumName)
            ).map { it.toPreviewInfo(link) }
                .catch { it.printStackTrace() }
                .let { flow -> lifecycle?.let { flow.flowWithLifecycle(it) } ?: flow }
            is ThreadLink -> repository.observeThread(link.threadId)
                .map { it.toPreviewInfo(link) }
                .catch { it.printStackTrace() }
                .let { flow -> lifecycle?.let { flow.flowWithLifecycle(it) } ?: flow }
            is SimpleLink -> null
        }
        val fallback = flowOf(defaultPreview(link))
        return listOfNotNull(fallback, detailFlow).merge()
            .let { flow -> lifecycle?.let { flow.flowWithLifecycle(it) } ?: flow }
    }

    override fun fetchPreview(
        context: Context,
        link: ClipBoardLink,
        callback: (Result<PreviewInfo>) -> Unit
    ) {
        val provider: (suspend () -> Result<PreviewInfo>)? = when (link) {
            is ForumLink -> suspend { runCatching { repository.fetchForum(link.forumName) }.map { it.toPreviewInfo(link) } }
            is ThreadLink -> suspend { runCatching { repository.fetchThread(link.threadId) }.map { it.toPreviewInfo(link) } }
            is SimpleLink -> null
        }

        if (provider == null) {
            callback(Result.success(defaultPreview(link)))
            return
        }

        applicationScope.launch(dispatcherProvider.io) {
            val result = provider()
            withContext(dispatcherProvider.main) {
                callback(result)
            }
        }
    }

    private fun ForumPreviewData.toPreviewInfo(link: ForumLink): PreviewInfo =
        PreviewInfo(
            clipBoardLink = link,
            url = link.url,
            title = resourceProvider.getString(R.string.title_forum, name ?: link.forumName),
            subtitle = slogan ?: resourceProvider.getString(R.string.subtitle_link),
            icon = Icon(url = avatar)
        )

    private fun ThreadPreviewData.toPreviewInfo(link: ThreadLink): PreviewInfo =
        PreviewInfo(
            clipBoardLink = link,
            url = link.url,
            title = title ?: link.url,
            subtitle = resourceProvider.getString(
                R.string.subtitle_quick_preview_thread,
                forumName ?: "",
                (replyNum ?: 0L).toString()
            ),
            icon = authorPortrait?.let { Icon(url = StringUtil.getAvatarUrl(it)) } ?: Icon(resId = R.drawable.ic_link)
        )

    private fun defaultPreview(link: ClipBoardLink): PreviewInfo =
        PreviewInfo(
            clipBoardLink = link,
            url = link.url,
            title = link.url,
            subtitle = resourceProvider.getString(R.string.subtitle_link),
            icon = Icon(resId = R.drawable.ic_link)
        )
}
