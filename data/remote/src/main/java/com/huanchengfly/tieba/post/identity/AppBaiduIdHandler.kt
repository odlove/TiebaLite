package com.huanchengfly.tieba.post.identity

import com.huanchengfly.tieba.core.network.identity.BaiduIdHandler
import com.huanchengfly.tieba.core.runtime.client.ClientUtils
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class AppBaiduIdHandler @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope
) : BaiduIdHandler {
    override fun saveBaiduId(baiduId: String) {
        applicationScope.launch {
            ClientUtils.saveBaiduId(baiduId)
        }
    }
}
