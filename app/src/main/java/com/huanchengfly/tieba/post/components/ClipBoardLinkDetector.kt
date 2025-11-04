package com.huanchengfly.tieba.post.components

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.huanchengfly.tieba.core.runtime.clipboard.ClipboardContent
import com.huanchengfly.tieba.core.runtime.clipboard.ClipboardPreviewHandler
import com.huanchengfly.tieba.core.runtime.preview.PreviewInfo
import com.huanchengfly.tieba.core.runtime.preview.QuickPreviewService
import com.huanchengfly.tieba.post.MainActivityV2
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Singleton
class ClipBoardLinkDetector @Inject constructor(
    private val quickPreviewService: QuickPreviewService
) : ClipboardPreviewHandler {

    private val mutablePreviewInfoStateFlow = MutableStateFlow<PreviewInfo?>(null)
    val previewInfoStateFlow: StateFlow<PreviewInfo?> = mutablePreviewInfoStateFlow.asStateFlow()

    private var previewJob: Job? = null

    override fun shouldHandle(activity: Activity): Boolean = activity is LifecycleOwner

    override fun onClipboardContent(activity: Activity, content: ClipboardContent?): Boolean {
        if (activity !is LifecycleOwner) {
            clearPreview()
            return true
        }

        if (content == null) {
            clearPreview()
            return true
        }

        if (activity !is MainActivityV2) {
            clearPreview()
            return false
        }

        previewJob?.cancel()
        mutablePreviewInfoStateFlow.value = null
        previewJob = activity.lifecycleScope.launch {
            quickPreviewService.getPreviewFlow(activity, content.link, activity.lifecycle)
                .collect { mutablePreviewInfoStateFlow.value = it }
        }
        return true
    }

    fun clearPreview() {
        previewJob?.cancel()
        previewJob = null
        mutablePreviewInfoStateFlow.value = null
    }
}
