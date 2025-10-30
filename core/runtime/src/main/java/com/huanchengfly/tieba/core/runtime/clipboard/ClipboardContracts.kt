package com.huanchengfly.tieba.core.runtime.clipboard

import android.app.Activity

/**
 * Provides access to the current clipboard content and metadata.
 */
interface ClipboardReader {
    fun readText(): String?
    fun readTimestamp(): Long
}

/**
 * Consumes clipboard updates and drives feature-specific behaviour.
 */
interface ClipboardPreviewHandler {
    /**
     * Whether the handler should process clipboard updates for the given [activity].
     */
    fun shouldHandle(activity: Activity): Boolean = true

    /**
     * Called whenever clipboard content changes or needs to be cleared.
     *
     * @param activity The currently started [Activity].
     * @param text The clipboard text, or `null` if there is no actionable content.
     */
    /**
     * Handles clipboard content updates.
     *
     * @return `true` if the handler consumed the content and future duplicates can be ignored,
     * `false` if the monitor should re-check the same clipboard value for subsequent activities.
     */
    fun onClipboardContent(activity: Activity, text: String?): Boolean
}
