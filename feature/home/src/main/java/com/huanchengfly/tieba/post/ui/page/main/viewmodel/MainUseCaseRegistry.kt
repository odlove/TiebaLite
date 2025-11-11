package com.huanchengfly.tieba.post.ui.page.main

import dagger.MapKey
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@MapKey
annotation class MainIntentKey(val value: KClass<out MainUiIntent>)

interface MainIntentUseCase<I : MainUiIntent> {
    fun execute(intent: I): Flow<MainPartialChange>
}

@ViewModelScoped
class MainUseCaseRegistry @Inject constructor(
    private val useCases: Map<Class<out MainUiIntent>, @JvmSuppressWildcards Provider<MainIntentUseCase<out MainUiIntent>>>
) {
    companion object {
        private val REQUIRED_INTENTS = setOf(
            MainUiIntent.NewMessage.Receive::class.java,
            MainUiIntent.NewMessage.Clear::class.java,
        )
    }

    init {
        val missing = REQUIRED_INTENTS - useCases.keys
        check(missing.isEmpty()) {
            "MainUseCaseRegistry 缺少 UseCase: " + missing.joinToString { it.simpleName.orEmpty() }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <I : MainUiIntent> execute(intent: I): Flow<MainPartialChange> {
        val provider = useCases[intent::class.java]
            ?: error("未找到 ${intent::class.java.simpleName} 的 UseCase")
        return (provider.get() as MainIntentUseCase<I>).execute(intent)
    }
}
