package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.repository.ThreadOperationRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@ViewModelScoped
class DeletePostUseCase @Inject constructor(
    private val threadOperationRepository: ThreadOperationRepository
) : ThreadIntentUseCase<ThreadUiIntent.DeletePost> {
    override fun execute(intent: ThreadUiIntent.DeletePost): Flow<ThreadPartialChange> =
        threadOperationRepository
            .delPost(
                intent.forumId,
                intent.forumName,
                intent.threadId,
                intent.postId,
                intent.tbs,
                false,
                intent.deleteMyPost
            )
            .map { ThreadPartialChange.DeletePost.Success(intent.postId) as ThreadPartialChange }
            .catch {
                emit(
                    ThreadPartialChange.DeletePost.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
}

@ViewModelScoped
class DeleteThreadUseCase @Inject constructor(
    private val threadOperationRepository: ThreadOperationRepository
) : ThreadIntentUseCase<ThreadUiIntent.DeleteThread> {
    override fun execute(intent: ThreadUiIntent.DeleteThread): Flow<ThreadPartialChange> =
        threadOperationRepository
            .delThread(
                intent.forumId,
                intent.forumName,
                intent.threadId,
                intent.tbs,
                intent.deleteMyThread,
                false
            )
            .map { ThreadPartialChange.DeleteThread.Success as ThreadPartialChange }
            .catch {
                emit(
                    ThreadPartialChange.DeleteThread.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
}
