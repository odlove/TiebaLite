package com.huanchengfly.tieba.core.runtime.clipboard

import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultClipboardReader @Inject constructor(
    @ApplicationContext private val context: Context
) : ClipboardReader {

    private val manager: ClipboardManager
        get() = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override fun readText(): String? {
        val clip = manager.primaryClip ?: return null
        return clip.getItemAt(0)?.coerceToText(context)?.toString()
    }

    override fun readTimestamp(): Long {
        val description = manager.primaryClipDescription ?: return 0L
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            description.timestamp
        } else {
            try {
                val field = description.javaClass.getDeclaredField("mTimestamp")
                field.isAccessible = true
                field.getLong(description)
            } catch (_: Exception) {
                0L
            }
        }
    }
}
