package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.core.network.error.getErrorCode
import com.huanchengfly.tieba.core.network.error.getErrorMessage
import com.huanchengfly.tieba.post.repository.ThreadOperationRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@ViewModelScoped
class AddFavoriteUseCase @Inject constructor(
    private val threadOperationRepository: ThreadOperationRepository,
    private val threadMetaStore: ThreadMetaStore
) : ThreadIntentUseCase<ThreadUiIntent.AddFavorite> {
    override fun execute(intent: ThreadUiIntent.AddFavorite): Flow<ThreadPartialChange> {
        return threadOperationRepository
            .addStore(intent.threadId, intent.postId)
            .map { response ->
                if (response.errorCode == 0) {
                    val current = threadMetaStore.get(intent.threadId) ?: ThreadMeta()
                    threadMetaStore.updateFromServer(
                        intent.threadId,
                        current.copy(
                            collectStatus = true,
                            collectMarkPid = intent.postId
                        )
                    )
                    ThreadPartialChange.AddFavorite.Success(
                        intent.postId,
                        intent.floor
                    )
                } else {
                    ThreadPartialChange.AddFavorite.Failure(
                        response.errorCode,
                        response.errorMsg
                    )
                }
            }
            .onStart { emit(ThreadPartialChange.AddFavorite.Start) }
            .catch {
                emit(
                    ThreadPartialChange.AddFavorite.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
    }
}

@ViewModelScoped
class RemoveFavoriteUseCase @Inject constructor(
    private val threadOperationRepository: ThreadOperationRepository,
    private val threadMetaStore: ThreadMetaStore
) : ThreadIntentUseCase<ThreadUiIntent.RemoveFavorite> {
    override fun execute(intent: ThreadUiIntent.RemoveFavorite): Flow<ThreadPartialChange> {
        return threadOperationRepository
            .removeStore(intent.threadId, intent.forumId, intent.tbs)
            .map { response ->
                if (response.errorCode == 0) {
                    val current = threadMetaStore.get(intent.threadId) ?: ThreadMeta()
                    threadMetaStore.updateFromServer(
                        intent.threadId,
                        current.copy(
                            collectStatus = false,
                            collectMarkPid = 0
                        )
                    )
                    ThreadPartialChange.RemoveFavorite.Success
                } else {
                    ThreadPartialChange.RemoveFavorite.Failure(
                        response.errorCode,
                        response.errorMsg
                    )
                }
            }
            .onStart { emit(ThreadPartialChange.RemoveFavorite.Start) }
            .catch {
                emit(
                    ThreadPartialChange.RemoveFavorite.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
    }
}
