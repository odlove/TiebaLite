package com.huanchengfly.tieba.core.mvi

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.LazyThreadSafetyMode

interface PartialChangeProducer<Intent : UiIntent, PC : PartialChange<State>, State : UiState> {
    fun toPartialChangeFlow(intentFlow: Flow<Intent>): Flow<PC>
}

@Stable
abstract class BaseViewModel<
        Intent : UiIntent,
        PC : PartialChange<State>,
        State : UiState,
        Event : UiEvent
        >(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider
) :
    ViewModel() {

    var initialized = false

    private val _eventFlow: MutableSharedFlow<Event> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _commonEventFlow: MutableSharedFlow<CommonUiEvent> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val eventFlow: SharedFlow<Event> = _eventFlow.asSharedFlow()

    val commonEventFlow: SharedFlow<CommonUiEvent> = _commonEventFlow.asSharedFlow()

    @Deprecated("Use eventFlow or commonEventFlow instead.")
    val uiEventFlow: SharedFlow<Event> = eventFlow

    private val _intentFlow = MutableSharedFlow<Intent>(
        replay = 0,
        extraBufferCapacity = INTENT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val initialState: State by lazy(LazyThreadSafetyMode.NONE) { createInitialState() }

    private val partialChangeProducer: PartialChangeProducer<Intent, PC, State> by lazy(LazyThreadSafetyMode.NONE) {
        createPartialChangeProducer()
    }

    protected abstract fun createInitialState(): State
    protected abstract fun createPartialChangeProducer(): PartialChangeProducer<Intent, PC, State>

    val uiState by lazy(LazyThreadSafetyMode.NONE) {
        partialChangeProducer.toPartialChangeFlow(_intentFlow)
            .onEach {
                Log.i("ViewModel", "partialChange $it")
                val event = dispatchEvent(it)
                if (event != null) {
                    Log.i("ViewModel", "event $event")
                    emitUiEvent(event)
                }
            }
            .scan(initialState) { oldState, partialChange ->
                partialChange.reduce(oldState)
            }
            .distinctUntilChanged()
            .flowOn(dispatcherProvider.io)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialState)
    }

    protected open fun dispatchEvent(partialChange: PC): UiEvent? = null

    fun send(intent: Intent) {
        Log.i("ViewModel", "send $intent")
        if (!_intentFlow.tryEmit(intent)) {
            viewModelScope.launch {
                _intentFlow.emit(intent)
            }
        }
    }

    protected fun emitEvent(event: Event) {
        if (!_eventFlow.tryEmit(event)) {
            viewModelScope.launch { _eventFlow.emit(event) }
        }
    }

    protected fun emitCommonEvent(event: CommonUiEvent) {
        if (!_commonEventFlow.tryEmit(event)) {
            viewModelScope.launch { _commonEventFlow.emit(event) }
        }
    }

    private fun emitUiEvent(event: UiEvent) {
        when (event) {
            is CommonUiEvent -> emitCommonEvent(event)
            else -> {
                @Suppress("UNCHECKED_CAST")
                val typedEvent = event as? Event
                if (typedEvent != null) {
                    emitEvent(typedEvent)
                } else {
                    Log.w("ViewModel", "Unhandled UiEvent type: $event")
                }
            }
        }
    }

    private companion object {
        private const val INTENT_BUFFER_CAPACITY = 64
        private const val EVENT_BUFFER_CAPACITY = 32
    }
}
