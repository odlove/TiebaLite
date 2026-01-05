package com.huanchengfly.tieba.core.theme.runtime.entrypoints

import com.huanchengfly.tieba.core.theme.runtime.controller.ThemeController
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeBridge
import com.huanchengfly.tieba.core.theme.runtime.bridge.TranslucentBackgroundStore
import com.huanchengfly.tieba.core.common.theme.ThemeRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeRuntimeEntryPoint {
    fun themeController(): ThemeController
    fun themeBridge(): ThemeBridge
    fun translucentBackgroundStore(): TranslucentBackgroundStore
    fun themeRepository(): ThemeRepository
}
