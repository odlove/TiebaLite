package com.huanchengfly.tieba.post.ui.page.main.usecase

import com.huanchengfly.tieba.post.ui.page.main.contract.MainPartialChange
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiIntent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

class MessageUseCaseTest {
    @Test
    fun `receive message should emit receive partial change`() = runBlocking {
        val useCase = ReceiveMessageUseCase()
        val intent = MainUiIntent.NewMessage.Receive(messageCount = 7)

        val result = useCase.execute(intent).first()

        assert(result == MainPartialChange.NewMessage.Receive(7))
    }

    @Test
    fun `clear message should emit clear partial change`() = runBlocking {
        val useCase = ClearMessageUseCase()

        val result = useCase.execute(MainUiIntent.NewMessage.Clear).first()

        assert(result == MainPartialChange.NewMessage.Clear)
    }
}
