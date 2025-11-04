package com.huanchengfly.tieba.post.di.entrypoints

import com.huanchengfly.tieba.core.runtime.device.ScreenMetricsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ScreenMetricsEntryPoint {
    fun screenMetricsManager(): ScreenMetricsManager
}
