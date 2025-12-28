package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.common.repository.UserInteractionFacade
import com.huanchengfly.tieba.core.network.error.getErrorCode
import com.huanchengfly.tieba.core.network.error.getErrorMessage
import com.huanchengfly.tieba.post.repository.PbPageRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@ViewModelScoped
class AgreeThreadUseCase @Inject constructor(
    private val userInteractionRepository: UserInteractionFacade,
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.AgreeThread> {
    override fun execute(intent: ThreadUiIntent.AgreeThread): Flow<ThreadPartialChange> {
        var previousHasAgree = 0
        var previousAgreeNum = 0
        return userInteractionRepository
            .opAgree(
                intent.threadId.toString(),
                intent.postId.toString(),
                hasAgree = if (intent.agree) 0 else 1,
                objType = 3
            )
            .map { ThreadPartialChange.AgreeThread.Success(intent.agree) as ThreadPartialChange }
            .catch {
                pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                    meta.copy(
                        hasAgree = previousHasAgree,
                        agreeNum = previousAgreeNum
                    )
                }
                emit(
                    ThreadPartialChange.AgreeThread.Failure(
                        !intent.agree,
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
            .onStart {
                val currentEntity = pbPageRepository.threadFlow(intent.threadId).value
                if (currentEntity != null) {
                    previousHasAgree = currentEntity.meta.hasAgree
                    previousAgreeNum = currentEntity.meta.agreeNum
                    pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                        meta.copy(
                            hasAgree = if (intent.agree) 1 else 0,
                            agreeNum = if (intent.agree) meta.agreeNum + 1 else meta.agreeNum - 1
                        )
                    }
                }
                emit(ThreadPartialChange.AgreeThread.Start(intent.agree))
            }
    }
}

@ViewModelScoped
class AgreePostUseCase @Inject constructor(
    private val userInteractionRepository: UserInteractionFacade,
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.AgreePost> {
    override fun execute(intent: ThreadUiIntent.AgreePost): Flow<ThreadPartialChange> {
        var previousHasAgree = 0
        var previousAgreeNum = 0
        return userInteractionRepository
            .opAgree(
                intent.threadId.toString(),
                intent.postId.toString(),
                hasAgree = if (intent.agree) 0 else 1,
                objType = 1
            )
            .map { ThreadPartialChange.AgreePost.Success(intent.postId, intent.agree) as ThreadPartialChange }
            .catch {
                pbPageRepository.updatePostMeta(intent.threadId, intent.postId) { meta ->
                    meta.copy(
                        hasAgree = previousHasAgree,
                        agreeNum = previousAgreeNum
                    )
                }
                emit(
                    ThreadPartialChange.AgreePost.Failure(
                        intent.postId,
                        !intent.agree,
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
            .onStart {
                val currentPost = pbPageRepository.postFlow(intent.threadId, intent.postId).value
                if (currentPost != null) {
                    previousHasAgree = currentPost.meta.hasAgree
                    previousAgreeNum = currentPost.meta.agreeNum
                    pbPageRepository.updatePostMeta(intent.threadId, intent.postId) { meta ->
                        meta.copy(
                            hasAgree = if (intent.agree) 1 else 0,
                            agreeNum = if (intent.agree) meta.agreeNum + 1 else meta.agreeNum - 1
                        )
                    }
                }
                emit(ThreadPartialChange.AgreePost.Start(intent.postId, intent.agree))
            }
    }
}
