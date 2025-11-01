package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.network.account.AccountCredentialsSource
import com.huanchengfly.tieba.core.network.account.AccountTokenProvider
import com.huanchengfly.tieba.core.network.device.DeviceConfigProvider
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.core.network.identity.BaiduIdHandler
import com.huanchengfly.tieba.core.network.identity.ClientIdentityProvider
import com.huanchengfly.tieba.core.network.identity.ClientIdentityRegistry
import com.huanchengfly.tieba.core.runtime.identity.DeviceIdentityProvider
import com.huanchengfly.tieba.core.runtime.identity.DeviceIdentityRegistry
import com.huanchengfly.tieba.core.runtime.identity.UuidStorage
import com.huanchengfly.tieba.post.account.RemoteAccountTokenProvider
import com.huanchengfly.tieba.post.device.AppDeviceConfigProvider
import com.huanchengfly.tieba.post.device.AppDeviceInfoProvider
import com.huanchengfly.tieba.post.identity.AppBaiduIdHandler
import com.huanchengfly.tieba.post.identity.AppClientIdentityProvider
import com.huanchengfly.tieba.post.identity.AppDeviceIdentityProvider
import com.huanchengfly.tieba.post.identity.AppUuidStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkIdentityRemoteModule {

    @Binds
    @Singleton
    abstract fun bindBaiduIdHandler(
        handler: AppBaiduIdHandler
    ): BaiduIdHandler

    @Binds
    @Singleton
    abstract fun bindUuidStorage(
        storage: AppUuidStorage
    ): UuidStorage

    @Binds
    @Singleton
    abstract fun bindDeviceInfoProvider(
        provider: AppDeviceInfoProvider
    ): DeviceInfoProvider

    @Binds
    @Singleton
    abstract fun bindDeviceConfigProvider(
        provider: AppDeviceConfigProvider
    ): DeviceConfigProvider

    @Binds
    @Singleton
    abstract fun bindAccountTokenProvider(
        provider: RemoteAccountTokenProvider
    ): AccountTokenProvider
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkIdentityRemoteProviderModule {

    @Provides
    @Singleton
    fun provideClientIdentityProvider(
        provider: AppClientIdentityProvider
    ): ClientIdentityProvider {
        ClientIdentityRegistry.registerFallback(provider)
        return provider
    }

    @Provides
    @Singleton
    fun provideDeviceIdentityProvider(
        provider: AppDeviceIdentityProvider
    ): DeviceIdentityProvider {
        DeviceIdentityRegistry.register(provider)
        return provider
    }
}
