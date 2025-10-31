package com.huanchengfly.tieba.core.network.runtime

import com.huanchengfly.tieba.core.network.account.AccountTokenProvider
import com.huanchengfly.tieba.core.network.device.DeviceConfigProvider
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.core.network.error.ErrorMessageProvider
import com.huanchengfly.tieba.core.network.identity.BaiduIdHandler
import com.huanchengfly.tieba.core.network.identity.ClientIdentityProvider

interface NetworkInitializer {
    fun registerAccountTokens(provider: AccountTokenProvider)
    fun registerClientIdentity(provider: ClientIdentityProvider)
    fun registerBaiduIdHandler(handler: BaiduIdHandler)
    fun registerDeviceInfo(provider: DeviceInfoProvider)
    fun registerDeviceConfig(provider: DeviceConfigProvider)
    fun registerErrorMessages(provider: ErrorMessageProvider)
    fun registerNetworkStatus(provider: NetworkStatusProvider)
    fun registerKzMode(provider: KzModeProvider)
    fun registerSignSecret(provider: SignSecretProvider)
    fun registerCookieProvider(provider: () -> String?)
}
