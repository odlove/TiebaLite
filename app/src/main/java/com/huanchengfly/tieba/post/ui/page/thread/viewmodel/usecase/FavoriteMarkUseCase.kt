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
class UpdateFavoriteMarkUseCase @Inject constructor(
    private val threadOperationRepository: ThreadOperationRepository,
    private val threadMetaStore: ThreadMetaStore
) : ThreadIntentUseCase<ThreadUiIntent.UpdateFavoriteMark> {
    override fun execute(intent: ThreadUiIntent.UpdateFavoriteMark): Flow<ThreadPartialChange> {
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
                    ThreadPartialChange.UpdateFavoriteMark.Success(intent.postId)
                } else {
                    ThreadPartialChange.UpdateFavoriteMark.Failure(
                        response.errorCode,
                        response.errorMsg
                    )
                }
            }
            .onStart { emit(ThreadPartialChange.UpdateFavoriteMark.Start) }
            .catch {
                emit(
                    ThreadPartialChange.UpdateFavoriteMark.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
    }
}
