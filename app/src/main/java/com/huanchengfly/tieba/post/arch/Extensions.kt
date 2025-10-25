package com.huanchengfly.tieba.post.arch

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.huanchengfly.tieba.core.common.collectIn
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.ui.pageViewModel as corePageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
inline fun <reified VM : BaseViewModel<*, *, *, *>> pageViewModel(
    key: String? = null,
): VM {
    val vm = corePageViewModel<VM>(key = key)
    val context = LocalContext.current
    if (context is BaseComposeActivity) {
        androidx.compose.runtime.DisposableEffect(vm) {
            val job = vm.commonEventFlow
                .collectIn(context) {
                    context.handleCommonEvent(it)
                }
            onDispose { job.cancel() }
        }
    }
    return vm
}

@Composable
inline fun <INTENT : UiIntent, reified VM : BaseViewModel<INTENT, *, *, *>> pageViewModel(
    initialIntent: List<INTENT> = emptyList(),
    key: String? = null,
): VM {
    val vm = pageViewModel<VM>(key)
    if (initialIntent.isNotEmpty()) {
        androidx.compose.runtime.LaunchedEffect(vm.initialized) {
            if (!vm.initialized) {
                vm.initialized = true
                initialIntent.asFlow()
                    .onEach(vm::send)
                    .flowOn(kotlinx.coroutines.Dispatchers.IO)
                    .launchIn(vm.viewModelScope)
            }
        }
    }
    return vm
}
