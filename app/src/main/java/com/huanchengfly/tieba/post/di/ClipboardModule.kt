package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.runtime.clipboard.ClipboardPreviewHandler
import com.huanchengfly.tieba.core.runtime.clipboard.ClipboardReader
import com.huanchengfly.tieba.post.components.ClipBoardLinkDetector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ClipboardModule {
    @Binds
    abstract fun bindClipboardReader(detector: ClipBoardLinkDetector): ClipboardReader

    @Binds
    abstract fun bindClipboardPreviewHandler(detector: ClipBoardLinkDetector): ClipboardPreviewHandler
}
