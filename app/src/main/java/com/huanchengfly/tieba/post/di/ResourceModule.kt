package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.post.resources.AndroidResourceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourceModule {
    @Binds
    @Singleton
    abstract fun bindResourceProvider(impl: AndroidResourceProvider): ResourceProvider
}
