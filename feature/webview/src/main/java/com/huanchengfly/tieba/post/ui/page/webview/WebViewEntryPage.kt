package com.huanchengfly.tieba.post.ui.page.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination(start = true)
@Composable
fun WebViewEntryPage(
    navigator: DestinationsNavigator,
) {
    LaunchedEffect(Unit) {
        navigator.navigateUp()
    }
}
