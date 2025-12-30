package com.huanchengfly.tieba.post.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.core.ui.R as CoreUiR

object TiebaUtil {
    private fun ClipData.setIsSensitive(isSensitive: Boolean): ClipData = apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, isSensitive)
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun copyText(
        context: Context,
        text: String?,
        toast: String = context.getString(CoreUiR.string.toast_copy_success),
        isSensitive: Boolean = false
    ) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Tieba Lite", text).setIsSensitive(isSensitive)
        cm.setPrimaryClip(clipData)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            context.toastShort(toast)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun shareText(context: Context, text: String, title: String? = null) {
        context.startActivity(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "${if (title != null) "「$title」\n" else ""}$text\n（分享自贴吧 Lite）"
            )
        })
    }
}
