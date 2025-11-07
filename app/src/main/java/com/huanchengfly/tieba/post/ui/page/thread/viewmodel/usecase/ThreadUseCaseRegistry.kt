package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.BuildConfig
import dagger.MapKey
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@MapKey
annotation class ThreadIntentKey(val value: KClass<out ThreadUiIntent>)

interface ThreadIntentUseCase<I : ThreadUiIntent> {
    fun execute(intent: I): Flow<ThreadPartialChange>
}

@ViewModelScoped
class ThreadUseCaseRegistry @Inject constructor(
    private val useCases: Map<Class<out ThreadUiIntent>, @JvmSuppressWildcards Provider<ThreadIntentUseCase<out ThreadUiIntent>>>
) {
    companion object {
        private val REQUIRED_INTENT_CLASSES: Set<Class<out ThreadUiIntent>> = setOf(
            ThreadUiIntent.Init::class.java,
            ThreadUiIntent.Load::class.java,
            ThreadUiIntent.LoadFirstPage::class.java,
            ThreadUiIntent.LoadMore::class.java,
            ThreadUiIntent.LoadPrevious::class.java,
            ThreadUiIntent.LoadLatestPosts::class.java,
            ThreadUiIntent.LoadMyLatestReply::class.java,
            ThreadUiIntent.ToggleImmersiveMode::class.java,
            ThreadUiIntent.AddFavorite::class.java,
            ThreadUiIntent.RemoveFavorite::class.java,
            ThreadUiIntent.AgreeThread::class.java,
            ThreadUiIntent.AgreePost::class.java,
            ThreadUiIntent.DeletePost::class.java,
            ThreadUiIntent.DeleteThread::class.java,
            ThreadUiIntent.UpdateFavoriteMark::class.java,
        )
    }

    init {
        if (BuildConfig.DEBUG) {
            val missing = REQUIRED_INTENT_CLASSES - useCases.keys
            check(missing.isEmpty()) {
                "ThreadUseCaseRegistry 缺少 UseCase: " + missing.joinToString { it.simpleName.orEmpty() }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <I : ThreadUiIntent> execute(intent: I): Flow<ThreadPartialChange> {
        val provider = useCases[intent::class.java]
            ?: error("未找到 ${intent::class.java.simpleName} 对应的 UseCase")
        val useCase = provider.get() as ThreadIntentUseCase<I>
        return useCase.execute(intent)
    }
}
