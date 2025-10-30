package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.runtime.client.ClientConfigRepository
import com.huanchengfly.tieba.post.data.client.ClientConfigRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ClientConfigModule {
    @Binds
    abstract fun bindClientConfigRepository(impl: ClientConfigRepositoryImpl): ClientConfigRepository
}
