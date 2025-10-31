package com.huanchengfly.tieba.core.network.runtime

import com.huanchengfly.tieba.core.network.account.AccountTokenProvider
import com.huanchengfly.tieba.core.network.account.AccountTokenRegistry
import com.huanchengfly.tieba.core.network.device.DeviceConfigProvider
import com.huanchengfly.tieba.core.network.device.DeviceConfigRegistry
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.core.network.device.DeviceInfoRegistry
import com.huanchengfly.tieba.core.network.error.ErrorMessageProvider
import com.huanchengfly.tieba.core.network.error.ErrorMessages
import com.huanchengfly.tieba.core.network.identity.BaiduIdHandler
import com.huanchengfly.tieba.core.network.identity.ClientIdentityProvider
import com.huanchengfly.tieba.core.network.identity.ClientIdentityRegistry
import com.huanchengfly.tieba.core.network.retrofit.interceptors.AddWebCookieInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.ConnectivityInterceptor

object DefaultNetworkInitializer : NetworkInitializer {
    override fun registerAccountTokens(provider: AccountTokenProvider) {
        AccountTokenRegistry.register(provider)
    }

    override fun registerClientIdentity(provider: ClientIdentityProvider) {
        ClientIdentityRegistry.register(provider)
    }

    override fun registerBaiduIdHandler(handler: BaiduIdHandler) {
        ClientIdentityRegistry.registerBaiduIdHandler(handler)
    }

    override fun registerDeviceInfo(provider: DeviceInfoProvider) {
        DeviceInfoRegistry.register(provider)
    }

    override fun registerDeviceConfig(provider: DeviceConfigProvider) {
        DeviceConfigRegistry.register(provider)
    }

    override fun registerErrorMessages(provider: ErrorMessageProvider) {
        ErrorMessages.register(provider)
    }

    override fun registerNetworkStatus(provider: NetworkStatusProvider) {
        ConnectivityInterceptor.registerNetworkStatusProvider(provider)
    }

    override fun registerKzMode(provider: KzModeProvider) {
        KzModeRegistry.register(provider)
    }

    override fun registerSignSecret(provider: SignSecretProvider) {
        SignSecretRegistry.register(provider)
    }

    override fun registerCookieProvider(provider: () -> String?) {
        AddWebCookieInterceptor.registerCookieProvider(provider)
    }
}
