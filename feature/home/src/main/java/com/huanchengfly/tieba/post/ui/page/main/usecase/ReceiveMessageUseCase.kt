package com.huanchengfly.tieba.post.ui.page.main.usecase

import com.huanchengfly.tieba.post.ui.page.main.contract.MainPartialChange
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiIntent
import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainIntentUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

@ViewModelScoped
class ReceiveMessageUseCase @Inject constructor() :
    MainIntentUseCase<MainUiIntent.NewMessage.Receive> {
    override fun execute(intent: MainUiIntent.NewMessage.Receive): Flow<MainPartialChange> =
        flow {
            emit(MainPartialChange.NewMessage.Receive(intent.messageCount))
        }.catch { emit(MainPartialChange.NewMessage.Receive(intent.messageCount)) }
}
