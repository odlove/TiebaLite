package com.huanchengfly.tieba.post.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.navigation.HomeNavigationActions
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.toastShort

fun launchUrl(
    context: Context,
    homeNavigation: HomeNavigationActions?,
    url: String,
) {
    val uri = Uri.parse(url)
    val host = uri.host
    val path = uri.path
    val scheme = uri.scheme
    if (host == null || scheme == null || path == null) {
        return
    }
    if (scheme == "tiebaclient") {
        when (uri.getQueryParameter("action")) {
            "preview_file" -> {
                val realUrl = uri.getQueryParameter("url")
                if (realUrl.isNullOrEmpty()) {
                    return
                }
                launchUrl(context, homeNavigation, realUrl)
            }

            else -> {
                context.toastShort(CoreUiR.string.toast_feature_unavailable)
            }
        }
        return
    }
    if (!path.contains("android_asset")) {
        if (path == "/mo/q/checkurl") {
            launchUrl(
                context,
                homeNavigation,
                uri.getQueryParameter("url")?.replace("http://https://", "https://").orEmpty()
            )
            return
        }
        if (host == "tieba.baidu.com" && path.startsWith("/p/")) {
            val threadId = path.substring(3).toLongOrNull()
            if (threadId != null) {
                if (homeNavigation != null) {
                    homeNavigation.openThread(threadId = threadId)
                    return
                }
            }
        }
        val isTiebaLink =
            host.contains("tieba.baidu.com") || host.contains("wappass.baidu.com") || host.contains(
                "ufosdk.baidu.com"
            ) || host.contains("m.help.baidu.com")
        if (isTiebaLink || context.appPreferences.useWebView) {
            if (homeNavigation != null) {
                homeNavigation.openWeb(url)
                return
            }
        }
        if (context.appPreferences.useCustomTabs) {
            val intentBuilder = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(ThemeColorResolver.topBarColor(context))
                        .build()
                )
            try {
                intentBuilder.build().launchUrl(context, uri)
            } catch (e: ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}
