package com.huanchengfly.tieba.data.local.di

import com.huanchengfly.tieba.data.local.history.HistoryDataSource
import com.huanchengfly.tieba.data.local.history.LitePalHistoryDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryModule {

    @Binds
    @Singleton
    abstract fun bindHistoryDataSource(
        impl: LitePalHistoryDataSource
    ): HistoryDataSource
}
