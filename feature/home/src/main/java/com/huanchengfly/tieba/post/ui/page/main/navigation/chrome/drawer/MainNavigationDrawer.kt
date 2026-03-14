package com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.drawer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.post.ui.page.main.navigation.compose.ThemeDrawerSheet

@Composable
fun PermanentNavigationDrawer(
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ThemeDrawerSheet(modifier = modifier) {
        Row(Modifier.fillMaxSize()) {
            drawerContent()
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}
