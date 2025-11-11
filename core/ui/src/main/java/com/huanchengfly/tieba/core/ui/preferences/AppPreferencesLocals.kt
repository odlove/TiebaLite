package com.huanchengfly.tieba.core.ui.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.huanchengfly.tieba.core.common.preferences.AppPreferencesDataSource

val LocalAppPreferences = staticCompositionLocalOf<AppPreferencesDataSource> {
    error("AppPreferences was not provided")
}

@Composable
fun ProvideAppPreferences(
    appPreferences: AppPreferencesDataSource,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppPreferences provides appPreferences, content = content)
}
