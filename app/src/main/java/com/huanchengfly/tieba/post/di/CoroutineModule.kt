package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.mvi.DefaultDispatcherProvider
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider
    }
}
