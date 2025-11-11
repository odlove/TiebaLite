package com.huanchengfly.tieba.post.ui.page.main.usecase

import com.huanchengfly.tieba.post.ui.page.main.MainPartialChange
import com.huanchengfly.tieba.post.ui.page.main.MainUiIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageUseCaseTest {

    @Test
    fun `ReceiveMessageUseCase emits Receive partial change`() = runTest {
        val useCase = ReceiveMessageUseCase()
        val intent = MainUiIntent.NewMessage.Receive(messageCount = 7)

        val result = useCase.execute(intent).first()

        assert(result == MainPartialChange.NewMessage.Receive(7))
    }

    @Test
    fun `ClearMessageUseCase emits Clear partial change`() = runTest {
        val useCase = ClearMessageUseCase()
        val result = useCase.execute(MainUiIntent.NewMessage.Clear).first()

        assert(result == MainPartialChange.NewMessage.Clear)
    }
}
