package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.repository.AccountRepository
import com.huanchengfly.tieba.post.repository.AccountRepositoryImpl
import com.huanchengfly.tieba.post.repository.AddPostRepository
import com.huanchengfly.tieba.post.repository.AddPostRepositoryImpl
import com.huanchengfly.tieba.post.repository.ContentModerationRepository
import com.huanchengfly.tieba.post.repository.ContentModerationRepositoryImpl
import com.huanchengfly.tieba.post.repository.ContentRecommendRepository
import com.huanchengfly.tieba.post.repository.ContentRecommendRepositoryImpl
import com.huanchengfly.tieba.post.repository.ForumInfoRepository
import com.huanchengfly.tieba.post.repository.ForumInfoRepositoryImpl
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import com.huanchengfly.tieba.post.repository.ForumOperationRepositoryImpl
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.FrsPageRepositoryImpl
import com.huanchengfly.tieba.post.repository.NotificationRepository
import com.huanchengfly.tieba.post.repository.NotificationRepositoryImpl
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.PhotoRepository
import com.huanchengfly.tieba.post.repository.PhotoRepositoryImpl
import com.huanchengfly.tieba.post.repository.PbPageRepositoryImpl
import com.huanchengfly.tieba.post.repository.PersonalizedRepository
import com.huanchengfly.tieba.post.repository.PersonalizedRepositoryImpl
import com.huanchengfly.tieba.post.repository.SearchRepository
import com.huanchengfly.tieba.post.repository.SearchRepositoryImpl
import com.huanchengfly.tieba.post.repository.SubPostsRepository
import com.huanchengfly.tieba.post.repository.SubPostsRepositoryImpl
import com.huanchengfly.tieba.post.repository.ThreadFeedRepository
import com.huanchengfly.tieba.post.repository.ThreadFeedRepositoryImpl
import com.huanchengfly.tieba.post.repository.ThreadOperationRepository
import com.huanchengfly.tieba.post.repository.ThreadOperationRepositoryImpl
import com.huanchengfly.tieba.post.repository.ThreadStoreRepository
import com.huanchengfly.tieba.post.repository.ThreadStoreRepositoryImpl
import com.huanchengfly.tieba.post.repository.UserContentRepository
import com.huanchengfly.tieba.post.repository.UserContentRepositoryImpl
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepositoryImpl
import com.huanchengfly.tieba.post.repository.UserProfileRepository
import com.huanchengfly.tieba.post.repository.UserProfileRepositoryImpl
import com.huanchengfly.tieba.post.repository.UserSocialRepository
import com.huanchengfly.tieba.post.repository.UserSocialRepositoryImpl
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
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository

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
    abstract fun bindContentModerationRepository(
        impl: ContentModerationRepositoryImpl
    ): ContentModerationRepository

    @Binds
    @Singleton
    abstract fun bindThreadOperationRepository(
        impl: ThreadOperationRepositoryImpl
    ): ThreadOperationRepository

    @Binds
    @Singleton
    abstract fun bindSubPostsRepository(
        impl: SubPostsRepositoryImpl
    ): SubPostsRepository

    @Binds
    @Singleton
    abstract fun bindThreadStoreRepository(
        impl: ThreadStoreRepositoryImpl
    ): ThreadStoreRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindUserSocialRepository(
        impl: UserSocialRepositoryImpl
    ): UserSocialRepository

    @Binds
    @Singleton
    abstract fun bindUserContentRepository(
        impl: UserContentRepositoryImpl
    ): UserContentRepository

    @Binds
    @Singleton
    abstract fun bindForumInfoRepository(
        impl: ForumInfoRepositoryImpl
    ): ForumInfoRepository

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        impl: PhotoRepositoryImpl
    ): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindThreadFeedRepository(
        impl: ThreadFeedRepositoryImpl
    ): ThreadFeedRepository
}
