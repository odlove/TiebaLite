package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.network.account.AccountTokenProvider
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.core.network.device.DeviceConfigProvider
import com.huanchengfly.tieba.core.network.error.ErrorMessageProvider
import com.huanchengfly.tieba.core.network.identity.BaiduIdHandler
import com.huanchengfly.tieba.core.network.identity.ClientIdentityProvider
import com.huanchengfly.tieba.core.network.identity.ClientIdentityRegistry
import com.huanchengfly.tieba.core.network.runtime.KzModeProvider
import com.huanchengfly.tieba.post.account.AppAccountTokenProvider
import com.huanchengfly.tieba.post.device.AppDeviceInfoProvider
import com.huanchengfly.tieba.post.device.AppDeviceConfigProvider
import com.huanchengfly.tieba.post.error.AppErrorMessageProvider
import com.huanchengfly.tieba.post.identity.AppClientIdentityProvider
import com.huanchengfly.tieba.post.identity.AppBaiduIdHandler
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
    abstract fun bindAccountTokenProvider(provider: AppAccountTokenProvider): AccountTokenProvider

    @Binds
    abstract fun bindDeviceInfoProvider(provider: AppDeviceInfoProvider): DeviceInfoProvider

    @Binds
    abstract fun bindErrorMessageProvider(provider: AppErrorMessageProvider): ErrorMessageProvider

    @Binds
    abstract fun bindDeviceConfigProvider(provider: AppDeviceConfigProvider): DeviceConfigProvider

    @Binds
    abstract fun bindBaiduIdHandler(handler: AppBaiduIdHandler): BaiduIdHandler

    @Binds
    abstract fun bindKzModeProvider(provider: AppKzModeProvider): KzModeProvider
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkIdentityProviderModule {
    @Provides
    @Singleton
    fun provideClientIdentityProvider(
        provider: AppClientIdentityProvider
    ): ClientIdentityProvider {
        ClientIdentityRegistry.registerFallback(provider)
        return provider
    }
}
