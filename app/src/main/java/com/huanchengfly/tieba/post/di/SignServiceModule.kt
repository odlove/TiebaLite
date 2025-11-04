package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.runtime.service.sign.SignNotificationController
import com.huanchengfly.tieba.core.runtime.service.sign.SignTaskRunner
import com.huanchengfly.tieba.post.services.sign.AppSignNotificationController
import com.huanchengfly.tieba.post.services.sign.AppSignTaskRunner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SignServiceModule {
    @Binds
    @Singleton
    abstract fun bindSignTaskRunner(impl: AppSignTaskRunner): SignTaskRunner

    @Binds
    @Singleton
    abstract fun bindSignNotificationController(impl: AppSignNotificationController): SignNotificationController
}
