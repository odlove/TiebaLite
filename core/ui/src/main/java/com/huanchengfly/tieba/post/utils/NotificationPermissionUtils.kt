package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.toastShort

fun Context.requestNotificationPermissionIfNeeded(
    @StringRes descriptionResId: Int,
    @StringRes deniedResId: Int,
) {
    if (!shouldRequestNotificationPermission()) {
        return
    }
    appPreferences.notificationPermissionRequested = true
    requestPermission {
        permissions = listOf(PermissionUtils.POST_NOTIFICATIONS)
        description = getString(descriptionResId)
        onDenied = {
            toastShort(deniedResId)
        }
    }
}

private fun Context.shouldRequestNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return false
    }
    if (!AccountUtil.isLoggedIn()) {
        return false
    }
    if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
        return false
    }
    if (appPreferences.notificationPermissionRequested) {
        return false
    }
    return true
}
