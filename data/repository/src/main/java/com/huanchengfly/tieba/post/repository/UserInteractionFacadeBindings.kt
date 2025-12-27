package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.repository.UserInteractionFacade
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserInteractionFacadeBindings {
    @Binds
    abstract fun bindUserInteractionFacade(
        impl: UserInteractionFacadeImpl
    ): UserInteractionFacade
}
