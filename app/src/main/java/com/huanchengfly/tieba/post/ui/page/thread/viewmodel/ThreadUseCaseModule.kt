package com.huanchengfly.tieba.post.ui.page.thread

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(ViewModelComponent::class)
abstract class ThreadUseCaseModule {
    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.Init::class)
    abstract fun bindThreadInitUseCase(useCase: ThreadInitUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.Load::class)
    abstract fun bindLoadThreadUseCase(useCase: LoadThreadUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.LoadFirstPage::class)
    abstract fun bindLoadFirstPageUseCase(useCase: LoadFirstPageUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.LoadMore::class)
    abstract fun bindLoadMoreUseCase(useCase: LoadMoreUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.LoadPrevious::class)
    abstract fun bindLoadPreviousUseCase(useCase: LoadPreviousUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.LoadLatestPosts::class)
    abstract fun bindLoadLatestPostsUseCase(useCase: LoadLatestPostsUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.LoadMyLatestReply::class)
    abstract fun bindLoadMyLatestReplyUseCase(useCase: LoadMyLatestReplyUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.ToggleImmersiveMode::class)
    abstract fun bindToggleImmersiveModeUseCase(useCase: ToggleImmersiveModeUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.AddCollect::class)
    abstract fun bindAddCollectUseCase(useCase: AddCollectUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.RemoveCollect::class)
    abstract fun bindRemoveCollectUseCase(useCase: RemoveCollectUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.UpdateCollectMark::class)
    abstract fun bindUpdateCollectMarkUseCase(useCase: UpdateCollectMarkUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.AgreeThread::class)
    abstract fun bindAgreeThreadUseCase(useCase: AgreeThreadUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.AgreePost::class)
    abstract fun bindAgreePostUseCase(useCase: AgreePostUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.DeletePost::class)
    abstract fun bindDeletePostUseCase(useCase: DeletePostUseCase): ThreadIntentUseCase<out ThreadUiIntent>

    @Binds
    @IntoMap
    @ThreadIntentKey(ThreadUiIntent.DeleteThread::class)
    abstract fun bindDeleteThreadUseCase(useCase: DeleteThreadUseCase): ThreadIntentUseCase<out ThreadUiIntent>
}
