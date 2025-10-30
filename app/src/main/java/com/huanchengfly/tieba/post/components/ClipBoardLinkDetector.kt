package com.huanchengfly.tieba.post.components

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.huanchengfly.tieba.core.runtime.clipboard.ClipboardPreviewHandler
import com.huanchengfly.tieba.core.runtime.clipboard.ClipboardReader
import com.huanchengfly.tieba.post.MainActivityV2
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil
import com.huanchengfly.tieba.post.utils.getClipBoardText
import com.huanchengfly.tieba.post.utils.getClipBoardTimestamp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

open class ClipBoardLink(
    val url: String,
)

class ClipBoardForumLink(
    url: String,
    val forumName: String,
) : ClipBoardLink(url)

class ClipBoardThreadLink(
    url: String,
    val threadId: String,
) : ClipBoardLink(url)

@Singleton
class ClipBoardLinkDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quickPreviewUtil: QuickPreviewUtil
) : ClipboardReader, ClipboardPreviewHandler {

    private val mutablePreviewInfoStateFlow = MutableStateFlow<QuickPreviewUtil.PreviewInfo?>(null)
    val previewInfoStateFlow: StateFlow<QuickPreviewUtil.PreviewInfo?> = mutablePreviewInfoStateFlow.asStateFlow()

    private var previewJob: Job? = null

    override fun readText(): String? = context.getClipBoardText()
    override fun readTimestamp(): Long = context.getClipBoardTimestamp()

    override fun shouldHandle(activity: Activity): Boolean = activity is BaseActivity

    override fun onClipboardContent(activity: Activity, text: String?): Boolean {
        if (activity !is LifecycleOwner) {
            clearPreview()
            return true
        }

        if (text.isNullOrBlank()) {
            clearPreview()
            return true
        }

        val url = text.extractFirstUrl() ?: run {
            clearPreview()
            return true
        }

        val link = parseLink(url) ?: run {
            clearPreview()
            return true
        }

        if (activity !is MainActivityV2) {
            clearPreview()
            return false
        }

        previewJob?.cancel()
        previewJob = activity.lifecycleScope.launch {
            quickPreviewUtil.getPreviewInfoFlow(activity, link, activity.lifecycle)
                .collect { mutablePreviewInfoStateFlow.value = it }
        }
        return true
    }

    fun clearPreview() {
        previewJob?.cancel()
        previewJob = null
        mutablePreviewInfoStateFlow.value = null
    }

    private fun parseLink(url: String): ClipBoardLink? {
        val uri = Uri.parse(url)
        if (!isTiebaDomain(uri.host)) {
            return null
        }
        val path = uri.path
        return when {
            path.isNullOrEmpty() -> null
            path.startsWith("/p/") -> ClipBoardThreadLink(url, path.substring(3))
            path.equals("/f", ignoreCase = true) || path.equals("/mo/q/m", ignoreCase = true) -> {
                val kw = uri.getQueryParameter("kw")
                val word = uri.getQueryParameter("word")
                val kz = uri.getQueryParameter("kz")

                when {
                    !kw.isNullOrEmpty() -> ClipBoardForumLink(url, kw)
                    !word.isNullOrEmpty() -> ClipBoardForumLink(url, word)
                    !kz.isNullOrEmpty() -> ClipBoardThreadLink(url, kz)
                    else -> null
                }
            }

            else -> ClipBoardLink(url)
        }
    }

    private fun isTiebaDomain(host: String?): Boolean {
        return host != null && (host.equals("wapp.baidu.com", ignoreCase = true) ||
                host.equals("tieba.baidu.com", ignoreCase = true) ||
                host.equals("tiebac.baidu.com", ignoreCase = true))
    }

    private fun String.extractFirstUrl(): String? {
        val matcher = URL_PATTERN.matcher(this)
        return if (matcher.find()) matcher.group() else null
    }

    companion object {
        private val URL_PATTERN = Pattern.compile(
            "((http|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}(?:\\.[0-9]{1,3}){3}))(:[0-9]{1,4})*(/[a-zA-Z0-9&%_./-~-]*)?"
        )
    }
}
