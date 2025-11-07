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
class AddFavoriteUseCase @Inject constructor(
    private val threadOperationRepository: ThreadOperationRepository,
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.AddFavorite> {
    override fun execute(intent: ThreadUiIntent.AddFavorite): Flow<ThreadPartialChange> {
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
                    ThreadPartialChange.AddFavorite.Success(
                        intent.postId,
                        intent.floor
                    )
                } else {
                    pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                        meta.copy(
                            collectStatus = previousCollectStatus,
                            collectMarkPid = previousCollectMarkPid
                        )
                    }
                    ThreadPartialChange.AddFavorite.Failure(
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
                emit(ThreadPartialChange.AddFavorite.Start)
            }
            .catch {
                pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                    meta.copy(
                        collectStatus = previousCollectStatus,
                        collectMarkPid = previousCollectMarkPid
                    )
                }
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
    private val pbPageRepository: PbPageRepository
) : ThreadIntentUseCase<ThreadUiIntent.RemoveFavorite> {
    override fun execute(intent: ThreadUiIntent.RemoveFavorite): Flow<ThreadPartialChange> {
        var previousCollectStatus = 0
        var previousCollectMarkPid = 0L
        return threadOperationRepository
            .removeStore(intent.threadId, intent.forumId, intent.tbs)
            .map { response ->
                if (response.errorCode == 0) {
                    pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                        meta.copy(
                            collectStatus = 0,
                            collectMarkPid = 0
                        )
                    }
                    ThreadPartialChange.RemoveFavorite.Success
                } else {
                    pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                        meta.copy(
                            collectStatus = previousCollectStatus,
                            collectMarkPid = previousCollectMarkPid
                        )
                    }
                    ThreadPartialChange.RemoveFavorite.Failure(
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
                        collectStatus = 0,
                        collectMarkPid = 0
                    )
                }
                emit(ThreadPartialChange.RemoveFavorite.Start)
            }
            .catch {
                pbPageRepository.updateThreadMeta(intent.threadId) { meta ->
                    meta.copy(
                        collectStatus = previousCollectStatus,
                        collectMarkPid = previousCollectMarkPid
                    )
                }
                emit(
                    ThreadPartialChange.RemoveFavorite.Failure(
                        it.getErrorCode(),
                        it.getErrorMessage()
                    )
                )
            }
    }
}
