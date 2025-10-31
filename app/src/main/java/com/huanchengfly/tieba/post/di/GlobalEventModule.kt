package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.mvi.GlobalEventBus
import com.huanchengfly.tieba.core.mvi.SharedFlowGlobalEventBus
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
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
        @ApplicationScope scope: CoroutineScope
    ): GlobalEventBus = SharedFlowGlobalEventBus(scope)
}
