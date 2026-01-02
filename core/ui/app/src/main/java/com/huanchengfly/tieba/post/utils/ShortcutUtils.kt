package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.LoadResult
import com.github.panpf.sketch.request.execute

suspend fun requestPinShortcut(
    context: Context,
    shortcutId: String,
    iconImageUri: String,
    label: String,
    shortcutIntent: Intent,
    onSuccess: () -> Unit = {},
    onFailure: (String) -> Unit = {}
) {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        val imageResult = LoadRequest(context, iconImageUri).execute()
        if (imageResult is LoadResult.Success) {
            val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
                .setIcon(IconCompat.createWithBitmap(imageResult.bitmap))
                .setIntent(shortcutIntent)
                .setShortLabel(label)
                .build()
            val result = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
            if (result) onSuccess()
            else onFailure(context.getString(CoreUiR.string.launcher_not_support_pin_shortcut))
        } else {
            onFailure(context.getString(CoreUiR.string.load_shortcut_icon_fail))
        }
    } else {
        onFailure(context.getString(CoreUiR.string.launcher_not_support_pin_shortcut))
    }
}
