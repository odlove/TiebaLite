package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.network.account.AccountCredentialsSource
import com.huanchengfly.tieba.core.network.error.ErrorMessageProvider
import com.huanchengfly.tieba.core.runtime.device.ScreenMetricsProvider
import com.huanchengfly.tieba.core.runtime.device.ScreenMetricsManager
import com.huanchengfly.tieba.core.network.runtime.KzModeProvider
import com.huanchengfly.tieba.post.account.AppAccountCredentialsSource
import com.huanchengfly.tieba.post.error.AppErrorMessageProvider
import com.huanchengfly.tieba.post.runtime.AppKzModeProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkIdentityModule {
    @Binds
    abstract fun bindAccountCredentialsSource(provider: AppAccountCredentialsSource): AccountCredentialsSource

    @Binds
    abstract fun bindErrorMessageProvider(provider: AppErrorMessageProvider): ErrorMessageProvider

    @Binds
    abstract fun bindKzModeProvider(provider: AppKzModeProvider): KzModeProvider
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkIdentityProviderModule {
    @Provides
    @Singleton
    fun provideScreenMetricsProvider(manager: ScreenMetricsManager): ScreenMetricsProvider = manager.provider
}
