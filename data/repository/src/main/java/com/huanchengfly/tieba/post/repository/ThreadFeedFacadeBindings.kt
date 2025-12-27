package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.repository.ThreadFeedFacade
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ThreadFeedFacadeBindings {
    @Binds
    abstract fun bindThreadFeedFacade(
        impl: ThreadFeedFacadeImpl
    ): ThreadFeedFacade
}
