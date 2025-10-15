package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.repository.AddPostRepository
import com.huanchengfly.tieba.post.repository.AddPostRepositoryImpl
import com.huanchengfly.tieba.post.repository.ContentRecommendRepository
import com.huanchengfly.tieba.post.repository.ContentRecommendRepositoryImpl
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import com.huanchengfly.tieba.post.repository.ForumOperationRepositoryImpl
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.FrsPageRepositoryImpl
import com.huanchengfly.tieba.post.repository.NotificationRepository
import com.huanchengfly.tieba.post.repository.NotificationRepositoryImpl
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.PbPageRepositoryImpl
import com.huanchengfly.tieba.post.repository.PersonalizedRepository
import com.huanchengfly.tieba.post.repository.PersonalizedRepositoryImpl
import com.huanchengfly.tieba.post.repository.SearchRepository
import com.huanchengfly.tieba.post.repository.SearchRepositoryImpl
import com.huanchengfly.tieba.post.repository.ThreadOperationRepository
import com.huanchengfly.tieba.post.repository.ThreadOperationRepositoryImpl
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 依赖注入模块
 *
 * 使用 @Binds 注解绑定接口到实现类，相比 @Provides 更高效
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPbPageRepository(
        impl: PbPageRepositoryImpl
    ): PbPageRepository

    @Binds
    @Singleton
    abstract fun bindFrsPageRepository(
        impl: FrsPageRepositoryImpl
    ): FrsPageRepository

    @Binds
    @Singleton
    abstract fun bindPersonalizedRepository(
        impl: PersonalizedRepositoryImpl
    ): PersonalizedRepository

    @Binds
    @Singleton
    abstract fun bindAddPostRepository(
        impl: AddPostRepositoryImpl
    ): AddPostRepository

    @Binds
    @Singleton
    abstract fun bindUserInteractionRepository(
        impl: UserInteractionRepositoryImpl
    ): UserInteractionRepository

    @Binds
    @Singleton
    abstract fun bindForumOperationRepository(
        impl: ForumOperationRepositoryImpl
    ): ForumOperationRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindContentRecommendRepository(
        impl: ContentRecommendRepositoryImpl
    ): ContentRecommendRepository

    @Binds
    @Singleton
    abstract fun bindThreadOperationRepository(
        impl: ThreadOperationRepositoryImpl
    ): ThreadOperationRepository
}
