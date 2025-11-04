package com.huanchengfly.tieba.core.runtime.clipboard

import android.app.Activity
import com.huanchengfly.tieba.core.runtime.preview.ClipBoardLink

/**
 * Provides access to the current clipboard content and metadata.
 */
interface ClipboardReader {
    fun readText(): String?
    fun readTimestamp(): Long
}

/**
 * Represents actionable clipboard information extracted from the system clipboard.
 */
data class ClipboardContent(
    val rawText: String,
    val link: ClipBoardLink
)

/**
 * Parses raw clipboard text into structured clipboard content.
 */
interface ClipboardContentParser {
    fun parse(text: String): ClipboardContent?
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
     * Handles clipboard content updates.
     *
     * @param activity The currently started [Activity].
     * @param content Structured clipboard content, or `null` if there is no actionable content.
     *
     * @return `true` if the handler consumed the content and future duplicates can be ignored,
     * `false` if the monitor should re-check the same clipboard value for subsequent activities.
     */
    fun onClipboardContent(activity: Activity, content: ClipboardContent?): Boolean
}
