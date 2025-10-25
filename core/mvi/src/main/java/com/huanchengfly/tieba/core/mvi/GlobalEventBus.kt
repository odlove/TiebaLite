package com.huanchengfly.tieba.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

interface GlobalEventBus {
    val events: SharedFlow<UiEvent>
    fun emit(event: UiEvent)
    suspend fun emitSuspend(event: UiEvent)
    fun tryEmit(event: UiEvent): Boolean
}

class SharedFlowGlobalEventBus(
    private val scope: CoroutineScope,
    private val extraBufferCapacity: Int = DEFAULT_BUFFER_CAPACITY,
    private val onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST
) : GlobalEventBus {
    private val internalFlow: MutableSharedFlow<UiEvent> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = extraBufferCapacity,
        onBufferOverflow = onBufferOverflow
    )

    override val events: SharedFlow<UiEvent> = internalFlow.asSharedFlow()

    override fun emit(event: UiEvent) {
        if (!internalFlow.tryEmit(event)) {
            scope.launch { internalFlow.emit(event) }
        }
    }

    override suspend fun emitSuspend(event: UiEvent) {
        internalFlow.emit(event)
    }

    override fun tryEmit(event: UiEvent): Boolean = internalFlow.tryEmit(event)

    private companion object {
        private const val DEFAULT_BUFFER_CAPACITY = 32
    }
}

val LocalGlobalEventBus = staticCompositionLocalOf<GlobalEventBus> {
    error("GlobalEventBus not provided")
}

fun GlobalEventBus.eventsFlow(): SharedFlow<UiEvent> = events

inline fun <reified Event : UiEvent> GlobalEventBus.eventsOf(): Flow<Event> =
    events.filterIsInstance<Event>()

fun GlobalEventBus.emitGlobalEvent(event: UiEvent) {
    emit(event)
}

suspend fun GlobalEventBus.emitGlobalEventSuspend(event: UiEvent) {
    emitSuspend(event)
}

fun CoroutineScope.emitGlobalEvent(bus: GlobalEventBus, event: UiEvent) {
    if (!bus.tryEmit(event)) {
        launch { bus.emitSuspend(event) }
    }
}

inline fun <reified Event : UiEvent> GlobalEventBus.onEvent(
    scope: CoroutineScope,
    noinline filter: (Event) -> Boolean = { true },
    noinline listener: suspend (Event) -> Unit,
): Job {
    return scope.launch {
        eventsOf<Event>()
            .filter(filter)
            .cancellable()
            .collect { listener(it) }
    }
}

@Composable
inline fun <reified Event : UiEvent> GlobalEventBus.onEvent(
    noinline filter: (Event) -> Boolean = { true },
    noinline listener: suspend (Event) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(filter, listener) {
        val job = onEvent(coroutineScope, filter, listener)
        onDispose { job.cancel() }
    }
}

@Composable
inline fun <reified Event : UiEvent> onGlobalEvent(
    bus: GlobalEventBus = LocalGlobalEventBus.current,
    noinline filter: (Event) -> Boolean = { true },
    noinline listener: suspend (Event) -> Unit,
) {
    bus.onEvent(filter, listener)
}
