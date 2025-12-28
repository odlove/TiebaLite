package com.huanchengfly.tieba.post.ui.page.reply

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.core.common.reply.AddPostResult
import com.huanchengfly.tieba.core.common.reply.UploadImageResult
import com.huanchengfly.tieba.core.network.error.getErrorCode
import com.huanchengfly.tieba.core.network.error.getErrorMessage
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.GlobalEventBus
import com.huanchengfly.tieba.core.mvi.GlobalEvent
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.core.mvi.emitGlobalEventSuspend
import com.huanchengfly.tieba.core.common.preferences.AppPreferencesDataSource
import com.huanchengfly.tieba.post.components.ImageUploader
import com.huanchengfly.tieba.post.repository.AddPostRepository
import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.post.utils.FileUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

enum class ReplyPanelType {
    NONE,
    EMOJI,
    IMAGE,
    VOICE
}

@Stable
@HiltViewModel
class ReplyViewModel @Inject constructor(
    private val addPostRepository: AddPostRepository,
    private val globalEventBus: GlobalEventBus,
    dispatcherProvider: DispatcherProvider,
    private val resourceProvider: ResourceProvider,
    @ApplicationContext private val context: android.content.Context,
    private val appPreferences: AppPreferencesDataSource
) :
    BaseViewModel<ReplyUiIntent, ReplyPartialChange, ReplyUiState, ReplyUiEvent>(dispatcherProvider) {
    override fun createInitialState() = ReplyUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ReplyUiIntent, ReplyPartialChange, ReplyUiState> =
        ReplyPartialChangeProducer()

    override fun dispatchEvent(partialChange: ReplyPartialChange): UiEvent? = when (partialChange) {
        is ReplyPartialChange.Send.Success -> ReplyUiEvent.ReplySuccess(
            partialChange.threadId,
            partialChange.postId,
            partialChange.expInc
        )

        is ReplyPartialChange.UploadImages.Success -> ReplyUiEvent.UploadSuccess(partialChange.resultList)

        is ReplyPartialChange.Send.Failure -> CommonUiEvent.Toast(
            resourceProvider.getString(
                R.string.toast_reply_failed,
                partialChange.errorCode,
                partialChange.errorMessage
            )
        )

        is ReplyPartialChange.UploadImages.Failure -> CommonUiEvent.Toast(
            resourceProvider.getString(
                R.string.toast_upload_image_failed,
                partialChange.errorMessage
            )
        )

        else -> null
    }

    private inner class ReplyPartialChangeProducer :
        PartialChangeProducer<ReplyUiIntent, ReplyPartialChange, ReplyUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ReplyUiIntent>): Flow<ReplyPartialChange> =
            merge(
                intentFlow.filterIsInstance<ReplyUiIntent.UploadImages>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.Send>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.SwitchPanel>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.AddImage>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.RemoveImage>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ReplyUiIntent.ToggleIsOriginImage>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun ReplyUiIntent.Send.producePartialChange(): Flow<ReplyPartialChange.Send> {
            return addPostRepository
                .addPost(
                    content,
                    forumId,
                    forumName,
                    threadId,
                    tbs,
                    postId = postId,
                    subPostId = subPostId,
                    replyUserId = replyUserId
                )
                .onEach { response ->
                    val newPostId = response.postId
                    globalEventBus.emitGlobalEventSuspend(
                        if (postId != null) {
                            GlobalEvent.ReplySuccess(
                                threadId,
                                postId,
                                postId,
                                subPostId,
                                newPostId
                            )
                        } else {
                            GlobalEvent.ReplySuccess(threadId, newPostId)
                        }
                    )
                }
                .map<AddPostResult, ReplyPartialChange.Send> {
                    ReplyPartialChange.Send.Success(
                        threadId = it.threadId,
                        postId = it.postId,
                        expInc = it.expInc
                    )
                }
                .onStart { emit(ReplyPartialChange.Send.Start) }
                .catch {
                    Log.i("ReplyViewModel", "failure: ${it.message}")
                    it.printStackTrace()
                    emit(ReplyPartialChange.Send.Failure(it.getErrorCode(), it.getErrorMessage()))
                }
        }

        private fun ReplyUiIntent.UploadImages.producePartialChange() =
            ImageUploader(forumName, appPreferences)
                .uploadImages(
                    imageUris.map {
                        FileUtil.getRealPathFromUri(
                            context,
                            Uri.parse(it)
                        )
                    },
                    isOriginImage
                )
                .map<List<UploadImageResult>, ReplyPartialChange.UploadImages> {
                    ReplyPartialChange.UploadImages.Success(it)
                }
                .onStart { emit(ReplyPartialChange.UploadImages.Start) }
                .catch {
                    it.printStackTrace()
                    emit(
                        ReplyPartialChange.UploadImages.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        private fun ReplyUiIntent.SwitchPanel.producePartialChange() =
            flowOf(ReplyPartialChange.SwitchPanel(panelType))

        private fun ReplyUiIntent.AddImage.producePartialChange() =
            flowOf(ReplyPartialChange.AddImage(imageUris))

        private fun ReplyUiIntent.RemoveImage.producePartialChange() =
            flowOf(ReplyPartialChange.RemoveImage(imageIndex))

        private fun ReplyUiIntent.ToggleIsOriginImage.producePartialChange() =
            flowOf(ReplyPartialChange.ToggleIsOriginImage(isOriginImage))
    }
}

sealed interface ReplyUiIntent : UiIntent {
    data class UploadImages(
        val forumName: String,
        val imageUris: List<String>,
        val isOriginImage: Boolean
    ) : ReplyUiIntent

    data class Send(
        val content: String,
        val forumId: Long,
        val forumName: String,
        val threadId: Long,
        val tbs: String,
        val postId: Long? = null,
        val subPostId: Long? = null,
        val replyUserId: Long? = null,
    ) : ReplyUiIntent

    data class SwitchPanel(val panelType: ReplyPanelType) : ReplyUiIntent

    data class AddImage(val imageUris: List<String>) : ReplyUiIntent

    data class RemoveImage(val imageIndex: Int) : ReplyUiIntent

    data class ToggleIsOriginImage(val isOriginImage: Boolean) : ReplyUiIntent
}

sealed interface ReplyPartialChange : PartialChange<ReplyUiState> {
    sealed class UploadImages : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState = when (this) {
            is Start -> oldState.copy(isUploading = true)
            is Success -> oldState.copy(
                isUploading = false,
                uploadImageResultList = resultList.toImmutableList()
            )

            is Failure -> oldState.copy(isUploading = false)
        }

        object Start : UploadImages()

        data class Success(val resultList: List<UploadImageResult>) : UploadImages()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : UploadImages()
    }

    sealed class Send : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState {
            return when (this) {
                is Start -> oldState.copy(isSending = true)
                is Success -> oldState.copy(isSending = false, replySuccess = true)
                is Failure -> oldState.copy(isSending = false, replySuccess = false)
            }
        }

        object Start : Send()

        data class Success(
            val threadId: Long,
            val postId: Long,
            val expInc: String
        ) : Send()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : Send()
    }

    data class SwitchPanel(val panelType: ReplyPanelType) : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState =
            oldState.copy(replyPanelType = panelType)
    }

    data class AddImage(val imageUris: List<String>) : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState =
            oldState.copy(selectedImageList = (oldState.selectedImageList + imageUris).toImmutableList())
    }

    data class RemoveImage(val imageIndex: Int) : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState =
            oldState.copy(selectedImageList = (oldState.selectedImageList - oldState.selectedImageList[imageIndex]).toImmutableList())
    }

    data class ToggleIsOriginImage(val isOriginImage: Boolean) : ReplyPartialChange {
        override fun reduce(oldState: ReplyUiState): ReplyUiState =
            oldState.copy(isOriginImage = isOriginImage)
    }
}

data class ReplyUiState(
    val isSending: Boolean = false,
    val replySuccess: Boolean = false,
    val replyPanelType: ReplyPanelType = ReplyPanelType.NONE,

    val isUploading: Boolean = false,
    val isOriginImage: Boolean = false,
    val selectedImageList: ImmutableList<String> = persistentListOf(),
    val uploadImageResultList: ImmutableList<UploadImageResult> = persistentListOf(),
) : UiState

sealed interface ReplyUiEvent : UiEvent {
    data class UploadSuccess(val resultList: List<UploadImageResult>) : ReplyUiEvent

    data class ReplySuccess(
        val threadId: Long,
        val postId: Long,
        val expInc: String
    ) : ReplyUiEvent
}
