package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.core.network.exception.TiebaException
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.ClipBoardForumLink
import com.huanchengfly.tieba.post.components.ClipBoardLink
import com.huanchengfly.tieba.post.components.ClipBoardThreadLink
import com.huanchengfly.tieba.post.interfaces.CommonCallback
import com.huanchengfly.tieba.post.preview.ForumPreviewData
import com.huanchengfly.tieba.post.preview.QuickPreviewRepository
import com.huanchengfly.tieba.post.preview.ThreadPreviewData
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
class QuickPreviewUtil @Inject constructor(
    private val quickPreviewRepository: QuickPreviewRepository,
    private val resourceProvider: ResourceProvider,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) {
    companion object {
        private fun isTiebaUrl(host: String?): Boolean {
            return host != null && (host.equals("wapp.baidu.com", ignoreCase = true) ||
                    host.equals("tieba.baidu.com", ignoreCase = true) ||
                    host.equals("tiebac.baidu.com", ignoreCase = true))
        }

        @JvmStatic
        fun isForumUrl(uri: Uri?): Boolean {
            if (uri == null || uri.host == null || uri.path == null) {
                return false
            }
            val path = uri.path
            val kw = uri.getQueryParameter("kw")
            val word = uri.getQueryParameter("word")
            return (path.equals("/f", ignoreCase = true) || path.equals(
                "/mo/q/m",
                ignoreCase = true
            )) &&
                    kw != null || word != null
        }

        @JvmStatic
        fun isThreadUrl(uri: Uri?): Boolean {
            if (uri == null || uri.host == null || uri.path == null) {
                return false
            }
            val path = uri.path
            val kz = uri.getQueryParameter("kz")
            return (path.equals("/f", ignoreCase = true) || path.equals(
                "/mo/q/m",
                ignoreCase = true
            )) &&
                    kz != null || path?.startsWith("/p/") == true
        }

        @JvmStatic
        fun getForumName(uri: Uri?): String? {
            if (uri == null || uri.host == null || uri.path == null) {
                return null
            }
            val path = uri.path
            val kw = uri.getQueryParameter("kw")
            val word = uri.getQueryParameter("word")
            if (path.equals("/f", ignoreCase = true) || path.equals("/mo/q/m", ignoreCase = true)) {
                if (kw != null) {
                    return kw
                } else if (word != null) {
                    return word
                }
            }
            return null
        }
    }

    fun getPreviewInfoFlow(
        context: Context,
        clipBoardLink: ClipBoardLink,
        lifeCycle: Lifecycle? = null,
    ): Flow<PreviewInfo?> {
        val detailFlow: Flow<PreviewInfo>? = when (clipBoardLink) {
            is ClipBoardForumLink -> quickPreviewRepository
                .observeForum(
                    forumName = clipBoardLink.forumName,
                    sortType = getSortType(context, clipBoardLink.forumName)
                )
                .map { it.toPreviewInfo(clipBoardLink) }
                .catch { it.printStackTrace() }
                .let { flow -> if (lifeCycle != null) flow.flowWithLifecycle(lifeCycle) else flow }
            is ClipBoardThreadLink -> clipBoardLink.threadId.toLongOrNull()?.let { threadId ->
                quickPreviewRepository
                    .observeThread(threadId)
                    .map { it.toPreviewInfo(clipBoardLink) }
                    .catch { it.printStackTrace() }
                    .let { flow -> if (lifeCycle != null) flow.flowWithLifecycle(lifeCycle) else flow }
            }
            else -> null
        }
        val flow = flowOf(
            PreviewInfo(
                clipBoardLink = clipBoardLink,
                url = clipBoardLink.url,
                title = clipBoardLink.url,
                subtitle = resourceProvider.getString(R.string.subtitle_link),
                icon = Icon(R.drawable.ic_link)
            )
        )
        return listOfNotNull(flow, detailFlow).merge()
            .let { flow -> if (lifeCycle != null) flow.flowWithLifecycle(lifeCycle) else flow }
    }

    fun getPreviewInfo(
        context: Context,
        link: ClipBoardLink,
        callback: CommonCallback<PreviewInfo>,
    ) {
        when (link) {
            is ClipBoardForumLink -> fetchForumPreview(link, callback)
            is ClipBoardThreadLink -> fetchThreadPreview(link, callback)
            else -> callback.onSuccess(defaultPreview(link))
        }
    }

    private fun fetchThreadPreview(
        link: ClipBoardThreadLink,
        callback: CommonCallback<PreviewInfo>,
    ) {
        val threadId = link.threadId.toLongOrNull()
        if (threadId == null) {
            callback.onFailure(-1, "Invalid thread id")
            return
        }
        applicationScope.launch(dispatcherProvider.io) {
            val result = runCatching { quickPreviewRepository.fetchThread(threadId) }
            withContext(dispatcherProvider.main) {
                result.fold(
                    onSuccess = { data -> callback.onSuccess(data.toPreviewInfo(link)) },
                    onFailure = { error ->
                        val code = (error as? TiebaException)?.code ?: -1
                        callback.onFailure(code, error.message)
                    }
                )
            }
        }
    }

    private fun fetchForumPreview(
        link: ClipBoardForumLink,
        callback: CommonCallback<PreviewInfo>,
    ) {
        applicationScope.launch(dispatcherProvider.io) {
            val result = runCatching { quickPreviewRepository.fetchForum(link.forumName) }
            withContext(dispatcherProvider.main) {
                result.fold(
                    onSuccess = { data -> callback.onSuccess(data.toPreviewInfo(link)) },
                    onFailure = { error ->
                        val code = (error as? TiebaException)?.code ?: -1
                        callback.onFailure(code, error.message)
                    }
                )
            }
        }
    }

    private fun defaultPreview(link: ClipBoardLink): PreviewInfo =
        PreviewInfo(
            clipBoardLink = link,
            url = link.url,
            title = link.url,
            subtitle = resourceProvider.getString(R.string.subtitle_link),
            icon = Icon(R.drawable.ic_link)
        )

    private fun ThreadPreviewData.toPreviewInfo(
        link: ClipBoardThreadLink,
    ): PreviewInfo =
        PreviewInfo(
            clipBoardLink = link,
            url = link.url,
            title = title,
            subtitle = resourceProvider.getString(
                R.string.subtitle_quick_preview_thread,
                forumName.orEmpty(),
                replyNum?.toString() ?: "0"
            ),
            icon = Icon(StringUtil.getAvatarUrl(authorPortrait))
        )

    private fun ForumPreviewData.toPreviewInfo(
        link: ClipBoardForumLink,
    ): PreviewInfo =
        PreviewInfo(
            clipBoardLink = link,
            url = link.url,
            title = resourceProvider.getString(
                R.string.title_forum,
                name ?: link.forumName
            ),
            subtitle = slogan,
            icon = Icon(avatar)
        )

    @Immutable
    data class PreviewInfo(
        val clipBoardLink: ClipBoardLink,
        val url: String? = null,
        val title: String? = null,
        val subtitle: String? = null,
        val icon: Icon? = null,
    )

    @Immutable
    data class Icon(
        val type: Int,
        val url: String? = null,
        @DrawableRes
        val res: Int = 0,
    ) {

        constructor(url: String?) : this(
            type = TYPE_URL,
            url = url
        )

        constructor(@DrawableRes res: Int) : this(
            type = TYPE_DRAWABLE_RES,
            res = res
        )

        companion object {
            const val TYPE_DRAWABLE_RES = 0
            const val TYPE_URL = 1
        }
    }
}
