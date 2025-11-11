package com.huanchengfly.tieba.core.ui.widgets.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAddAccountHandler = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun ProvideAccountActions(
    onAddAccount: () -> Unit,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAddAccountHandler provides onAddAccount,
        content = content
    )
}
