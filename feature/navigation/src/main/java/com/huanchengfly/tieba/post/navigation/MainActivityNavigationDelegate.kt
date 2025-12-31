package com.huanchengfly.tieba.post.navigation

import android.content.Intent
import androidx.navigation.NavHostController
import com.huanchengfly.tieba.post.runtime.preview.ClipBoardLink
import com.huanchengfly.tieba.core.ui.navigation.MainNavControllerHolder
import kotlinx.coroutines.CoroutineScope

class MainActivityNavigationDelegate(
    scope: CoroutineScope,
) {
    private val navControllerHolder = MainNavControllerHolder(scope)
    private val intentHandler = MainIntentHandler(navControllerHolder::navigate)
    private val clipBoardLinkNavigator by lazy {
        ClipBoardLinkNavigator { navControllerHolder.navController }
    }

    fun handleLaunchIntent(intent: Intent?) {
        intent?.let { intentHandler.handleIntent(it) }
    }

    fun handleNewIntent(intent: Intent) {
        if (!intentHandler.handleIntent(intent)) {
            navControllerHolder.navController?.handleDeepLink(intent)
        }
    }

    fun onNavControllerReady(navController: NavHostController) {
        navControllerHolder.navController = navController
    }

    fun openClipBoardLink(link: ClipBoardLink) {
        clipBoardLinkNavigator.open(link)
    }
}
