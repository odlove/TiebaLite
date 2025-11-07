package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.repository.PbPageRepository
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
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.UpdateFavoriteMark> {
    override fun execute(intent: ThreadUiIntent.UpdateFavoriteMark): Flow<ThreadPartialChange> {
        var previousCollectStatus = 0
        var previousCollectMarkPid = 0L
        return threadOperationRepository
            .addStore(intent.threadId, intent.postId)
            .map { response ->
                if (response.errorCode == 0) {
                    pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                        meta.copy(
                            collectStatus = 1,
                            collectMarkPid = intent.postId
                        )
                    }
                    ThreadPartialChange.UpdateFavoriteMark.Success(intent.postId)
                } else {
                    pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                        meta.copy(
                            collectStatus = previousCollectStatus,
                            collectMarkPid = previousCollectMarkPid
                        )
                    }
                    ThreadPartialChange.UpdateFavoriteMark.Failure(
                        response.errorCode,
                        response.errorMsg
                    )
                }
            }
            .onStart {
                pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                    previousCollectStatus = meta.collectStatus
                    previousCollectMarkPid = meta.collectMarkPid
                    meta.copy(
                        collectStatus = 1,
                        collectMarkPid = intent.postId
                    )
                }
                emit(ThreadPartialChange.UpdateFavoriteMark.Start)
            }
            .catch {
                pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                    meta.copy(
                        collectStatus = previousCollectStatus,
                        collectMarkPid = previousCollectMarkPid
                    )
                }
                emit(
                    ThreadPartialChange.UpdateFavoriteMark.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
    }
}
