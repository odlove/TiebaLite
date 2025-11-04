package com.huanchengfly.tieba.core.runtime.preview

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.huanchengfly.tieba.core.runtime.clipboard.ClipboardPreviewHandler
import kotlinx.coroutines.flow.Flow

interface QuickPreviewService {
    fun getPreviewFlow(
        context: Context,
        link: ClipBoardLink,
        lifecycle: Lifecycle? = null
    ): Flow<PreviewInfo?>

    fun fetchPreview(
        context: Context,
        link: ClipBoardLink,
        callback: (Result<PreviewInfo>) -> Unit
    )
}

sealed interface ClipBoardLink {
    val url: String
}

data class ForumLink(override val url: String, val forumName: String) : ClipBoardLink
data class ThreadLink(override val url: String, val threadId: Long) : ClipBoardLink
data class SimpleLink(override val url: String) : ClipBoardLink

data class PreviewInfo(
    val clipBoardLink: ClipBoardLink,
    val url: String,
    val title: String,
    val subtitle: String,
    val icon: Icon
)

data class Icon(
    val resId: Int? = null,
    val url: String? = null
)
