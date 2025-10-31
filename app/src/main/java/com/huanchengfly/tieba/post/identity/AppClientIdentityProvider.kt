package com.huanchengfly.tieba.post.identity

import com.huanchengfly.tieba.core.network.identity.ClientIdentityProvider
import com.huanchengfly.tieba.post.utils.ClientUtils
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
}
