package com.huanchengfly.tieba.post.ui.page.thread

import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ViewModelScoped
class ToggleImmersiveModeUseCase @Inject constructor() :
    ThreadIntentUseCase<ThreadUiIntent.ToggleImmersiveMode> {
    override fun execute(intent: ThreadUiIntent.ToggleImmersiveMode): Flow<ThreadPartialChange> =
        flowOf(ThreadPartialChange.ToggleImmersiveMode.Success(intent.isImmersiveMode))
}
