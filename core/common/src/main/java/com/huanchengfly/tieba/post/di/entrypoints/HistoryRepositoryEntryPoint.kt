package com.huanchengfly.tieba.post.di.entrypoints

import com.huanchengfly.tieba.core.common.history.HistoryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HistoryRepositoryEntryPoint {
    fun historyRepository(): HistoryRepository
}
