package com.huanchengfly.tieba.core.ui.theme.runtime.entrypoints

import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeBridge
import com.huanchengfly.tieba.core.ui.theme.runtime.TranslucentBackgroundStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeRuntimeEntryPoint {
    fun themeController(): ThemeController
    fun themeBridge(): ThemeBridge
    fun translucentBackgroundStore(): TranslucentBackgroundStore
}
