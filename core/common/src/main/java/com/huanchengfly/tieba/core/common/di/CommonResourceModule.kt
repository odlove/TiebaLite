package com.huanchengfly.tieba.core.common.di

import com.huanchengfly.tieba.core.common.AndroidResourceProvider
import com.huanchengfly.tieba.core.common.ResourceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonResourceModule {
    @Binds
    @Singleton
    abstract fun bindResourceProvider(impl: AndroidResourceProvider): ResourceProvider
}
