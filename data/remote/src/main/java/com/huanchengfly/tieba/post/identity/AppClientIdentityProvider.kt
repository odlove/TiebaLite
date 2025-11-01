package com.huanchengfly.tieba.post.identity

import com.huanchengfly.tieba.core.network.identity.ClientIdentityProvider
import com.huanchengfly.tieba.core.runtime.client.ClientUtils
import com.huanchengfly.tieba.core.runtime.identity.DeviceIdentityRegistry
import javax.inject.Inject

class AppClientIdentityProvider @Inject constructor() : ClientIdentityProvider {
    override val clientId: String?
        get() = ClientUtils.clientId

    override val sampleId: String?
        get() = ClientUtils.sampleId

    override val baiduId: String?
        get() = ClientUtils.baiduId

    override val activeTimestamp: Long
        get() = ClientUtils.activeTimestamp

    override val finalCuid: String?
        get() = DeviceIdentityRegistry.current.finalCuid

    override val newCuid: String?
        get() = DeviceIdentityRegistry.current.newCuid

    override val aid: String?
        get() = DeviceIdentityRegistry.current.aid

    override val androidId: String?
        get() = DeviceIdentityRegistry.current.getAndroidId("").takeIf { it.isNotEmpty() }
}
