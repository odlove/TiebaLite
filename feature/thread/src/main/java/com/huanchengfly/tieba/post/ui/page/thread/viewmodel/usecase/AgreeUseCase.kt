package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.core.common.repository.UserInteractionFacade
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.core.network.error.getErrorCode
import com.huanchengfly.tieba.core.network.error.getErrorMessage
import com.huanchengfly.tieba.post.models.mappers.toThreadMeta
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
    private val threadMetaStore: ThreadMetaStore,
    private val pbPageRepository: PbPageRepository,
) : ThreadIntentUseCase<ThreadUiIntent.AgreeThread> {
    override fun execute(intent: ThreadUiIntent.AgreeThread): Flow<ThreadPartialChange> {
        return userInteractionRepository
            .opAgree(
                intent.threadId.toString(),
                intent.postId.toString(),
                hasAgree = if (intent.agree) 0 else 1,
                objType = 3
            )
            .map {
                val fallback = pbPageRepository.threadFlow(intent.threadId).value?.toThreadMeta()
                val current = threadMetaStore.get(intent.threadId)
                    ?: fallback
                    ?: ThreadMeta()
                val nextAgreeNum = if (intent.agree) current.agreeNum + 1 else (current.agreeNum - 1).coerceAtLeast(0)
                threadMetaStore.updateFromServer(
                    intent.threadId,
                    current.copy(
                        hasAgree = intent.agree,
                        agreeNum = nextAgreeNum
                    )
                )
                ThreadPartialChange.AgreeThread.Success(intent.agree) as ThreadPartialChange
            }
            .catch {
                emit(
                    ThreadPartialChange.AgreeThread.Failure(
                        intent.agree,
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
            .onStart { emit(ThreadPartialChange.AgreeThread.Start(intent.agree)) }
    }
}

@ViewModelScoped
class AgreePostUseCase @Inject constructor(
    private val userInteractionRepository: UserInteractionFacade,
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.AgreePost> {
    override fun execute(intent: ThreadUiIntent.AgreePost): Flow<ThreadPartialChange> {
        return userInteractionRepository
            .opAgree(
                intent.threadId.toString(),
                intent.postId.toString(),
                hasAgree = if (intent.agree) 0 else 1,
                objType = 1
            )
            .map {
                pbPageRepository.updatePostMeta(
                    threadId = intent.threadId,
                    postId = intent.postId,
                    hasAgree = intent.agree
                )
                ThreadPartialChange.AgreePost.Success(intent.postId, intent.agree) as ThreadPartialChange
            }
            .catch {
                emit(
                    ThreadPartialChange.AgreePost.Failure(
                        intent.postId,
                        !intent.agree,
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
            .onStart { emit(ThreadPartialChange.AgreePost.Start(intent.postId, intent.agree)) }
    }
}
