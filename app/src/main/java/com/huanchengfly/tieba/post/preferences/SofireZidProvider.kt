package com.huanchengfly.tieba.post.preferences

import com.huanchengfly.tieba.post.repository.ZidProvider
import com.huanchengfly.tieba.post.utils.SofireUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SofireZidProvider @Inject constructor() : ZidProvider {
    override fun fetchZid(): Flow<String> = SofireUtils.fetchZid()
}
