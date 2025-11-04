package com.huanchengfly.tieba.post.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.huanchengfly.tieba.core.common.preferences.LauncherIcons
import com.huanchengfly.tieba.post.preferences.appPreferences

object AppIconUtil {
    const val PREF_KEY_APP_ICON = "app_icon"

    fun setIcon(
        context: Context,
        icon: String? = null,
        isThemed: Boolean? = null,
    ) {
        val appPreferences = context.appPreferences
        val resolvedIcon = icon ?: appPreferences.appIcon ?: LauncherIcons.NEW_ICON
        val resolvedThemed = isThemed ?: appPreferences.useThemedIcon
        val useThemedIcon = resolvedThemed && LauncherIcons.SUPPORT_THEMED_ICON.contains(resolvedIcon)
        var newIcon = if (LauncherIcons.ICONS.contains(resolvedIcon)) {
            resolvedIcon
        } else LauncherIcons.DEFAULT_ICON
        if (useThemedIcon) {
            newIcon = LauncherIcons.THEMED_ICON_MAPPING[newIcon] ?: newIcon
        }
        LauncherIcons.ICONS.forEach {
            if (it == newIcon) {
                context.packageManager.enableComponent(ComponentName(context, it))
            } else {
                context.packageManager.disableComponent(ComponentName(context, it))
            }
        }
        context.packageManager.disableComponent(
            ComponentName(
                context,
                LauncherIcons.OLD_LAUNCHER_ICON
            )
        )
    }

    /**
     * 启用组件
     *
     * @param componentName 组件名
     */
    fun PackageManager.enableComponent(componentName: ComponentName) {
        setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * 禁用组件
     *
     * @param componentName 组件名
     */
    fun PackageManager.disableComponent(componentName: ComponentName) {
        setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
