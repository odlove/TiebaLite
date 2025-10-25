package com.huanchengfly.tieba.core.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.hilt.navigation.compose.hiltViewModel as androidxHiltViewModel
import com.huanchengfly.tieba.core.mvi.BaseViewModel

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

@Composable
inline fun <reified VM : BaseViewModel<*, *, *, *>> pageViewModel(
    key: String? = null,
): VM = hiltViewModel(key = key)
