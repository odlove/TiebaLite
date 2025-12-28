package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.interaction.DislikeRequest
import com.huanchengfly.tieba.core.common.repository.UserInteractionFacade
import com.huanchengfly.tieba.post.models.DislikeBean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class UserInteractionFacadeImpl @Inject constructor(
    private val userInteractionRepository: UserInteractionRepository
) : UserInteractionFacade {
    override fun opAgree(
        threadId: String,
        postId: String,
        hasAgree: Int,
        objType: Int
    ): Flow<Unit> =
        userInteractionRepository.opAgree(threadId, postId, hasAgree, objType)

    override fun submitDislike(request: DislikeRequest): Flow<Unit> =
        userInteractionRepository.submitDislike(
            DislikeBean(
                threadId = request.threadId,
                dislikeIds = request.dislikeIds,
                forumId = request.forumId,
                clickTime = request.clickTime,
                extra = request.extra
            )
        ).map { }
}
