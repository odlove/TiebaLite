package com.huanchengfly.tieba.post.ui.page

import com.huanchengfly.tieba.post.ui.page.history.historyDestinations
import com.huanchengfly.tieba.post.ui.page.main.notifications.homeDestinations
import com.huanchengfly.tieba.post.ui.page.threadcollect.threadcollectDestinations
import com.huanchengfly.tieba.post.ui.page.webview.webviewDestinations
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route

object AppNavGraphs {
    private val extraDestinations: List<DestinationSpec<*>> = listOf(
        historyDestinations,
        threadcollectDestinations,
        webviewDestinations,
        homeDestinations,
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
