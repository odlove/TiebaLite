package com.huanchengfly.tieba.post.identity

import com.huanchengfly.tieba.core.network.identity.BaiduIdHandler
import com.huanchengfly.tieba.post.di.CoroutineModule
import com.huanchengfly.tieba.post.utils.ClientUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBaiduIdHandler @Inject constructor(
    @CoroutineModule.ApplicationScope private val applicationScope: CoroutineScope
) : BaiduIdHandler {
    override fun saveBaiduId(baiduId: String) {
        applicationScope.launch {
            ClientUtils.saveBaiduId(baiduId)
        }
    }
}
