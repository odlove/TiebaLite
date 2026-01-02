package com.huanchengfly.tieba.post.ui.page.main.viewmodel

import androidx.lifecycle.ViewModel
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun onMessageReceived(messageCount: Int) {
        _uiState.update { it.copy(messageCount = messageCount) }
    }

    fun clearMessageCount() {
        _uiState.update { it.copy(messageCount = 0) }
    }
}
