package com.huanchengfly.tieba.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

val LocalNavigator = staticCompositionLocalOf<DestinationsNavigator> { error("No navigator is available") }

@Composable
fun ProvideNavigator(
    navigator: DestinationsNavigator,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNavigator provides navigator, content = content)
}
