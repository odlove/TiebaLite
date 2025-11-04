package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.runtime.preview.ClipBoardLink
import com.huanchengfly.tieba.core.runtime.preview.LinkParser
import com.huanchengfly.tieba.core.runtime.preview.QuickPreviewService
import com.huanchengfly.tieba.post.runtime.preview.AppLinkParser
import com.huanchengfly.tieba.post.runtime.preview.AppQuickPreviewService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreviewModule {
    @Binds
    abstract fun bindLinkParser(parser: AppLinkParser): LinkParser<ClipBoardLink>

    @Binds
    abstract fun bindQuickPreviewService(service: AppQuickPreviewService): QuickPreviewService
}
