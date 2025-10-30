package com.huanchengfly.tieba.core.runtime.clipboard

import android.app.Activity
import android.app.Application
import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes activity lifecycle events and dispatches clipboard updates to a [ClipboardPreviewHandler].
 */
@Singleton
class ClipboardMonitor @Inject constructor(
    private val clipboardReader: ClipboardReader,
    private val previewHandler: ClipboardPreviewHandler
) : Application.ActivityLifecycleCallbacks {

    private var lastClipboardSignature: String? = null
    private var pendingSignature: String? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    override fun onActivityStarted(activity: Activity) {
        if (!previewHandler.shouldHandle(activity)) return
        activity.window?.decorView?.post { evaluateClipboard(activity) }
    }

    private fun evaluateClipboard(activity: Activity) {
        val text = clipboardReader.readText()
        val timestamp = clipboardReader.readTimestamp()

        if (text.isNullOrBlank()) {
            if (lastClipboardSignature != null) {
                lastClipboardSignature = null
                pendingSignature = null
                previewHandler.onClipboardContent(activity, null)
            }
            return
        }

        val signature = computeSignature(timestamp, text)
        if (signature == lastClipboardSignature && signature != pendingSignature) {
            return
        }

        val handled = previewHandler.onClipboardContent(activity, text)
        if (handled) {
            pendingSignature = null
            lastClipboardSignature = signature
        } else {
            pendingSignature = signature
            lastClipboardSignature = signature
        }
    }

    private fun computeSignature(timestamp: Long, text: String): String =
        if (timestamp != 0L) {
            "$timestamp:${text.hashCode()}"
        } else {
            "0:${text.hashCode()}"
        }
}
