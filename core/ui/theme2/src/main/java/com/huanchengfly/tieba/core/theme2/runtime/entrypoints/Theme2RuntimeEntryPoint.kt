package com.huanchengfly.tieba.core.theme2.runtime.entrypoints

import com.huanchengfly.tieba.core.theme2.runtime.ThemeRuntime
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface Theme2RuntimeEntryPoint {
    fun themeRuntime(): ThemeRuntime
}
