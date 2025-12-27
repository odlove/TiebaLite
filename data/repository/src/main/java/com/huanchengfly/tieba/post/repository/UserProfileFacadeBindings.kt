package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.repository.UserProfileFacade
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserProfileFacadeBindings {
    @Binds
    abstract fun bindUserProfileFacade(
        impl: UserProfileFacadeImpl
    ): UserProfileFacade
}
