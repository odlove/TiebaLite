package com.huanchengfly.tieba.post.navigation

import com.huanchengfly.tieba.post.ui.page.history.historyDestinations
import com.huanchengfly.tieba.post.ui.page.hottopic.list.hottopicDestinations
import com.huanchengfly.tieba.post.ui.page.login.loginDestinations
import com.huanchengfly.tieba.post.ui.page.main.notifications.homeDestinations
import com.huanchengfly.tieba.post.ui.page.forum.forumDestinations
import com.huanchengfly.tieba.post.ui.page.user.userDestinations
import com.huanchengfly.tieba.post.ui.page.search.searchDestinations
import com.huanchengfly.tieba.post.ui.page.settings.settingsDestinations
import com.huanchengfly.tieba.post.ui.page.subposts.subpostsDestinations
import com.huanchengfly.tieba.post.ui.page.reply.replyDestinations
import com.huanchengfly.tieba.post.ui.page.thread.threadDestinations
import com.huanchengfly.tieba.post.ui.page.threadcollect.threadcollectDestinations
import com.huanchengfly.tieba.post.ui.page.webview.webviewDestinations
import com.huanchengfly.tieba.post.NavGraphs
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route

object AppNavGraphs {
    private val extraDestinations: List<DestinationSpec<*>> = listOf(
        historyDestinations,
        hottopicDestinations,
        threadcollectDestinations,
        subpostsDestinations,
        replyDestinations,
        threadDestinations,
        webviewDestinations,
        homeDestinations,
        forumDestinations,
        userDestinations,
        settingsDestinations,
        loginDestinations,
        searchDestinations,
    ).flatten()

    val root: NavGraphSpec = object : NavGraphSpec {
        override val route: String = NavGraphs.root.route
        override val startRoute: Route = NavGraphs.root.startRoute
        override val destinationsByRoute: Map<String, DestinationSpec<*>> =
            buildMap {
                putAll(NavGraphs.root.destinationsByRoute)
                extraDestinations.forEach { destination ->
                    put(destination.route, destination)
                }
            }
        override val nestedNavGraphs: List<NavGraphSpec> = NavGraphs.root.nestedNavGraphs
    }
}
