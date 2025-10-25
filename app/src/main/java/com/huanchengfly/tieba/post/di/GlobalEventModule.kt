package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.mvi.GlobalEventBus
import com.huanchengfly.tieba.core.mvi.SharedFlowGlobalEventBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlobalEventModule {

    @Provides
    @Singleton
    fun provideGlobalEventBus(
        @com.huanchengfly.tieba.post.di.CoroutineModule.ApplicationScope scope: CoroutineScope
    ): GlobalEventBus = SharedFlowGlobalEventBus(scope)
}
