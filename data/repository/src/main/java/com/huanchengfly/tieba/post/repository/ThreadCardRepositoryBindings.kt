package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.repository.ThreadCardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ThreadCardRepositoryBindings {
    @Binds
    abstract fun bindThreadCardRepository(
        impl: ThreadCardRepositoryImpl
    ): ThreadCardRepository
}
