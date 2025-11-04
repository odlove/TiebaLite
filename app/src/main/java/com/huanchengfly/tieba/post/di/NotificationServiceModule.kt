package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.runtime.service.notification.NotificationChannelConfigurator
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationFetcher
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationNavigator
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationRenderer
import com.huanchengfly.tieba.post.services.notification.AppNotificationChannelConfigurator
import com.huanchengfly.tieba.post.services.notification.AppNotificationFetcher
import com.huanchengfly.tieba.post.services.notification.AppNotificationNavigator
import com.huanchengfly.tieba.post.services.notification.AppNotificationRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationServiceModule {
    @Binds
    @Singleton
    abstract fun bindNotificationFetcher(impl: AppNotificationFetcher): NotificationFetcher

    @Binds
    @Singleton
    abstract fun bindNotificationRenderer(impl: AppNotificationRenderer): NotificationRenderer

    @Binds
    @Singleton
    abstract fun bindNotificationChannelConfigurator(impl: AppNotificationChannelConfigurator): NotificationChannelConfigurator

    @Binds
    @Singleton
    abstract fun bindNotificationNavigator(impl: AppNotificationNavigator): NotificationNavigator
}
