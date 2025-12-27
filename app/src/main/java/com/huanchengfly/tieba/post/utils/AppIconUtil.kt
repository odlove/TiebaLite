package com.huanchengfly.tieba.post.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.huanchengfly.tieba.core.common.preferences.LauncherIcons
import com.huanchengfly.tieba.post.preferences.appPreferences

object AppIconUtil {
    const val PREF_KEY_APP_ICON = "app_icon"

    fun applyIconSelection(
        context: Context,
        icon: String? = null,
        isThemed: Boolean? = null
    ) {
        val prefs = context.appPreferences
        val currentIcon = prefs.appIcon ?: LauncherIcons.NEW_ICON
        val currentThemed = prefs.useThemedIcon
        val targetIcon = icon ?: currentIcon
        val targetThemed = isThemed ?: currentThemed

        if (targetIcon == currentIcon && targetThemed == currentThemed) {
            return
        }

        prefs.appIcon = targetIcon
        prefs.useThemedIcon = targetThemed
        syncIconState(
            context = context,
            selection = IconSelection(
                icon = targetIcon,
                useThemedIcon = targetThemed
            )
        )
    }

    @Deprecated("改用 applyIconSelection 统一入口")
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
        syncIconState(
            context = context,
            selection = IconSelection(
                icon = newIcon,
                useThemedIcon = useThemedIcon
            )
        )
    }

    private fun syncIconState(
        context: Context,
        selection: IconSelection
    ) {
        val packageManager = context.packageManager
        val targetAlias = selection.resolvedAlias()
        val currentAlias = resolveEnabledAlias(context)

        if (currentAlias != null && currentAlias != targetAlias) {
            packageManager.disableComponent(ComponentName(context, currentAlias))
        }
        if (currentAlias != targetAlias || !isComponentEnabled(packageManager, ComponentName(context, targetAlias))) {
            packageManager.enableComponent(ComponentName(context, targetAlias))
        }
        disableLegacyLauncherIcon(context)
    }

    private fun resolveEnabledAlias(context: Context): String? {
        val packageManager = context.packageManager
        LauncherIcons.ICONS.forEach { alias ->
            val componentName = ComponentName(context, alias)
            val state = packageManager.getComponentEnabledSetting(componentName)
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return alias
            }
        }
        val prefs = context.appPreferences
        val fallbackSelection = IconSelection(
            icon = prefs.appIcon ?: LauncherIcons.NEW_ICON,
            useThemedIcon = prefs.useThemedIcon
        )
        return fallbackSelection.resolvedAlias()
    }

    private fun isComponentEnabled(packageManager: PackageManager, componentName: ComponentName): Boolean {
        return packageManager.getComponentEnabledSetting(componentName) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    private fun disableLegacyLauncherIcon(context: Context) {
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

    private data class IconSelection(
        val icon: String,
        val useThemedIcon: Boolean
    ) {
        fun resolvedAlias(): String {
            val sanitizedIcon = if (LauncherIcons.ICONS.contains(icon)) {
                icon
            } else {
                LauncherIcons.DEFAULT_ICON
            }
            val shouldUseThemedIcon =
                useThemedIcon && LauncherIcons.SUPPORT_THEMED_ICON.contains(sanitizedIcon)
            return if (shouldUseThemedIcon) {
                LauncherIcons.THEMED_ICON_MAPPING[sanitizedIcon] ?: sanitizedIcon
            } else {
                sanitizedIcon
            }
        }
    }
}
