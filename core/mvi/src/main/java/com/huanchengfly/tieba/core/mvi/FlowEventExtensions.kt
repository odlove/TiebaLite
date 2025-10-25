package com.huanchengfly.tieba.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.State
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

@Composable
inline fun <reified T : UiState, A> Flow<T>.collectPartialAsState(
    prop1: KProperty1<T, A>,
    initial: A,
): State<A> {
    return produceState(
        initialValue = initial,
        key1 = this,
        key2 = prop1,
        key3 = initial
    ) {
        this@collectPartialAsState
            .map { prop1.get(it) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }
}

@Composable
inline fun <reified Event : UiEvent> Flow<Event>.onEvent(
    noinline listener: suspend (Event) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(key1 = listener, key2 = this) {
        val job = coroutineScope.launch {
            this@onEvent
                .flowOn(Dispatchers.IO)
                .collect { coroutineScope.launch { listener(it) } }
        }

        onDispose { job.cancel() }
    }
}

@OptIn(InternalComposeApi::class)
@Composable
inline fun <reified Event : UiEvent> BaseViewModel<*, *, *, out UiEvent>.onEvent(
    noinline listener: suspend (Event) -> Unit
) {
    val applyContext = currentComposer.applyCoroutineContext
    val coroutineScope = remember(applyContext) { CoroutineScope(applyContext) }
    DisposableEffect(key1 = listener, key2 = this) {
        val job = coroutineScope.launch {
            eventFlow
                .filterIsInstance<Event>()
                .collect { coroutineScope.launch { listener(it) } }
        }

        onDispose { job.cancel() }
    }
}

@OptIn(InternalComposeApi::class)
@Composable
fun BaseViewModel<*, *, *, *>.onCommonEvent(
    listener: suspend (CommonUiEvent) -> Unit
) {
    val applyContext = currentComposer.applyCoroutineContext
    val coroutineScope = remember(applyContext) { CoroutineScope(applyContext) }
    DisposableEffect(key1 = listener, key2 = this) {
        val job = coroutineScope.launch {
            commonEventFlow.collect { coroutineScope.launch { listener(it) } }
        }

        onDispose { job.cancel() }
    }
}
