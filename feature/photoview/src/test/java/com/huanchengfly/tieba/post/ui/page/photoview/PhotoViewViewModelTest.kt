package com.huanchengfly.tieba.post.ui.page.photoview

import com.huanchengfly.tieba.core.common.photoview.PicPageItem
import com.huanchengfly.tieba.core.common.photoview.PicPageResult
import com.huanchengfly.tieba.core.ui.media.photoview.LoadPicPageData
import com.huanchengfly.tieba.core.ui.media.photoview.PhotoViewData
import com.huanchengfly.tieba.core.ui.media.photoview.PicItem
import com.huanchengfly.tieba.post.repository.PhotoRepository
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class PhotoViewViewModelTest : BaseViewModelTest() {
    private lateinit var fakeRepository: FakePhotoRepository

    @Before
    override fun setup() {
        super.setup()
        fakeRepository = FakePhotoRepository()
    }

    @Test
    fun init_withoutLoadData_usesLocalItems() = runTest(testDispatcher) {
        val data = PhotoViewData(
            data = null,
            picItems = listOf(
                PicItem(
                    picId = "p1",
                    picIndex = 1,
                    url = "big1",
                    originUrl = "o1",
                    showOriginBtn = true,
                    originSize = 0,
                    postId = 100L
                ),
                PicItem(
                    picId = "p2",
                    picIndex = 2,
                    url = "big2",
                    originUrl = "o2",
                    showOriginBtn = false,
                    originSize = 0,
                    postId = 200L
                )
            ),
            index = 1
        )

        val viewModel = PhotoViewViewModel(testDispatcherProvider, fakeRepository)
        val job = collectUiState(viewModel)

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.send(PhotoViewUiIntent.Init(data))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.totalAmount)
        assertEquals(2, state.data.size)
        assertEquals(1, state.initialIndex)
        assertFalse(state.hasNext)
        assertFalse(state.hasPrev)
        assertEquals("p1", state.data[0].picId)
        assertEquals("o1", state.data[0].originUrl)
        assertEquals("big1", state.data[0].url)
        assertEquals(1, state.data[0].overallIndex)
        assertEquals("p2", state.data[1].picId)
        assertEquals("o2", state.data[1].originUrl)
        assertEquals(null, state.data[1].url)
        assertEquals(2, state.data[1].overallIndex)

        job.cancelAndJoin()
    }

    @Test
    fun init_withLoadData_usesRepositoryResult() = runTest(testDispatcher) {
        val loadData = LoadPicPageData(
            forumId = 1L,
            forumName = "test",
            seeLz = false,
            objType = "thread",
            picId = "p1",
            picIndex = 1,
            threadId = 2L,
            postId = 0L,
            originUrl = null
        )
        val data = PhotoViewData(
            data = loadData,
            picItems = emptyList(),
            index = 0
        )

        fakeRepository.nextResult = PicPageResult(
            totalAmount = 2,
            items = listOf(
                PicPageItem(
                    picId = "p1",
                    originUrl = "o1",
                    bigUrl = "big1",
                    showOriginalBtn = true,
                    overallIndex = 1,
                    postId = 100L
                ),
                PicPageItem(
                    picId = "p2",
                    originUrl = "o2",
                    bigUrl = "big2",
                    showOriginalBtn = false,
                    overallIndex = 2,
                    postId = 200L
                )
            )
        )

        val viewModel = PhotoViewViewModel(testDispatcherProvider, fakeRepository)
        val job = collectUiState(viewModel)

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.send(PhotoViewUiIntent.Init(data))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.totalAmount)
        assertEquals(2, state.data.size)
        assertEquals(0, state.initialIndex)
        assertFalse(state.hasNext)
        assertFalse(state.hasPrev)
        assertEquals("p1", state.data[0].picId)
        assertEquals("big1", state.data[0].url)
        assertEquals(loadData, state.loadPicPageData)

        job.cancelAndJoin()
    }
}

private class FakePhotoRepository : PhotoRepository {
    var nextResult: PicPageResult = PicPageResult()

    override fun picPage(
        forumId: String,
        forumName: String,
        threadId: String,
        seeLz: Boolean,
        picId: String,
        picIndex: String,
        objType: String,
        prev: Boolean
    ): Flow<PicPageResult> = flowOf(nextResult)
}
