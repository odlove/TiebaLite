package com.huanchengfly.tieba.post.ui.page.main.usecase

import com.huanchengfly.tieba.post.ui.page.main.MainIntentUseCase
import com.huanchengfly.tieba.post.ui.page.main.MainPartialChange
import com.huanchengfly.tieba.post.ui.page.main.MainUiIntent
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
