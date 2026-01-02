package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainViewModel
import org.junit.Test

class MainViewModelTest {
    @Test
    fun `receive message should update state`() {
        val viewModel = MainViewModel()

        viewModel.onMessageReceived(messageCount = 5)

        val state = viewModel.uiState.value
        assert(state.messageCount == 5)
    }

    @Test
    fun `clear message should reset state`() {
        val viewModel = MainViewModel()

        viewModel.onMessageReceived(messageCount = 5)
        viewModel.clearMessageCount()

        val state = viewModel.uiState.value
        assert(state.messageCount == 0)
    }
}
