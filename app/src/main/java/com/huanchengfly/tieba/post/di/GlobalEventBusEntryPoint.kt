package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.mvi.GlobalEventBus
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GlobalEventBusEntryPoint {
    fun globalEventBus(): GlobalEventBus
}
