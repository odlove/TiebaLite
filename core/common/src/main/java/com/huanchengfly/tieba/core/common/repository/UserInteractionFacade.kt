package com.huanchengfly.tieba.core.common.repository

import com.huanchengfly.tieba.core.common.interaction.DislikeRequest
import kotlinx.coroutines.flow.Flow

interface UserInteractionFacade {
    fun opAgree(
        threadId: String,
        postId: String,
        hasAgree: Int,
        objType: Int
    ): Flow<Unit>

    fun submitDislike(request: DislikeRequest): Flow<Unit>
}
