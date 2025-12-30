package com.huanchengfly.tieba.post.ui.page

import androidx.compose.runtime.Composable
import com.huanchengfly.tieba.post.ui.page.main.MainPageContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@RootNavGraph(start = true)
@Destination(start = true)
@Composable
fun MainPage(
    navigator: DestinationsNavigator,
) {
    MainPageContent(navigator = navigator)
}
