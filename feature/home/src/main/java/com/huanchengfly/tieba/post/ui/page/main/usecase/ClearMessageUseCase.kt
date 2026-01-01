package com.huanchengfly.tieba.post.ui.page.main.usecase

import com.huanchengfly.tieba.post.ui.page.main.contract.MainPartialChange
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiIntent
import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainIntentUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ViewModelScoped
class ClearMessageUseCase @Inject constructor() :
    MainIntentUseCase<MainUiIntent.NewMessage.Clear> {
    override fun execute(intent: MainUiIntent.NewMessage.Clear): Flow<MainPartialChange> =
        flowOf(MainPartialChange.NewMessage.Clear)
}
