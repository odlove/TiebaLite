package com.huanchengfly.tieba.data.repository.di

import com.huanchengfly.tieba.data.repository.block.BlockedContentChecker
import com.huanchengfly.tieba.data.repository.block.DefaultBlockedContentChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BlockModule {

    @Binds
    @Singleton
    abstract fun bindBlockedContentChecker(
        impl: DefaultBlockedContentChecker
    ): BlockedContentChecker
}
