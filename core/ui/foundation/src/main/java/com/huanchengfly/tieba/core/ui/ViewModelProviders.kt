package com.huanchengfly.tieba.core.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel as androidxHiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.huanchengfly.tieba.core.common.collectIn
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
inline fun <reified VM : ViewModel> hiltViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
): VM {
    return if (viewModelStoreOwner is NavBackStackEntry) {
        androidxHiltViewModel<VM>(viewModelStoreOwner, key)
    } else {
        viewModel(viewModelStoreOwner, key = key)
    }
}

interface CommonUiEventHandler {
    fun handleCommonUiEvent(event: CommonUiEvent)
}

@Composable
inline fun <reified VM : BaseViewModel<*, *, *, *>> pageViewModel(
    key: String? = null,
): VM {
    val vm = hiltViewModel<VM>(key = key)
    val context = LocalContext.current
    val handler = context.findCommonUiEventHandler()
    val lifecycleOwner = LocalLifecycleOwner.current
    if (handler != null) {
        DisposableEffect(vm, lifecycleOwner) {
            val job = vm.commonEventFlow.collectIn(lifecycleOwner) { event ->
                handler.handleCommonUiEvent(event)
            }
            onDispose { job.cancel() }
        }
    }
    return vm
}

@PublishedApi
internal tailrec fun Context.findCommonUiEventHandler(): CommonUiEventHandler? {
    return when (this) {
        is CommonUiEventHandler -> this
        is ContextWrapper -> baseContext?.findCommonUiEventHandler()
        else -> null
    }
}


@Composable
inline fun <Intent : UiIntent, reified VM : BaseViewModel<Intent, *, *, *>> pageViewModel(
    initialIntent: List<Intent> = emptyList(),
    key: String? = null,
): VM {
    val vm = pageViewModel<VM>(key)
    if (initialIntent.isNotEmpty()) {
        LaunchedEffect(vm.initialized) {
            if (!vm.initialized) {
                vm.initialized = true
                initialIntent.asFlow()
                    .onEach(vm::send)
                    .flowOn(Dispatchers.IO)
                    .launchIn(vm.viewModelScope)
            }
        }
    }
    return vm
}
