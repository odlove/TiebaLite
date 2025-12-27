package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.repository.ForumRecommendRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ForumRecommendRepositoryBindings {
    @Binds
    abstract fun bindForumRecommendRepository(
        impl: ForumRecommendRepositoryImpl
    ): ForumRecommendRepository
}
