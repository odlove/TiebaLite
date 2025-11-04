package com.huanchengfly.tieba.post.di.entrypoints

import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.post.ui.common.theme.ThemeBridge
import com.huanchengfly.tieba.post.ui.common.theme.ThemeUiDelegate
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeControllerEntryPoint {
    fun themeController(): ThemeController
    fun themeBridge(): ThemeBridge
    fun themeUiDelegate(): ThemeUiDelegate
}
