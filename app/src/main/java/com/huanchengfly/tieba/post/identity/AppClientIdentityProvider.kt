package com.huanchengfly.tieba.post.identity

import com.huanchengfly.tieba.core.network.identity.ClientIdentityProvider
import com.huanchengfly.tieba.core.runtime.client.ClientUtils
import com.huanchengfly.tieba.post.utils.CuidUtils
import com.huanchengfly.tieba.post.utils.UIDUtil
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
        get() = UIDUtil.finalCUID

    override val newCuid: String?
        get() = CuidUtils.getNewCuid()

    override val aid: String?
        get() = UIDUtil.getAid()

    override val androidId: String?
        get() = UIDUtil.getAndroidId("").takeIf { it.isNotEmpty() }
}
