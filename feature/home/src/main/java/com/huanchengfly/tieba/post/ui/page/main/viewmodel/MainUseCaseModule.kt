package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.post.ui.page.main.usecase.ClearMessageUseCase
import com.huanchengfly.tieba.post.ui.page.main.usecase.ReceiveMessageUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(ViewModelComponent::class)
abstract class MainUseCaseModule {
    @Binds
    @IntoMap
    @MainIntentKey(MainUiIntent.NewMessage.Receive::class)
    abstract fun bindReceiveMessageUseCase(useCase: ReceiveMessageUseCase): MainIntentUseCase<out MainUiIntent>

    @Binds
    @IntoMap
    @MainIntentKey(MainUiIntent.NewMessage.Clear::class)
    abstract fun bindClearMessageUseCase(useCase: ClearMessageUseCase): MainIntentUseCase<out MainUiIntent>
}
