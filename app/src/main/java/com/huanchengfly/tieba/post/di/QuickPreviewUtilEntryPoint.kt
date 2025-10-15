package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.utils.QuickPreviewUtil
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for accessing QuickPreviewUtil in non-Hilt-managed classes
 *
 * Used by singleton objects (like ClipBoardLinkDetector) that cannot use
 * constructor injection but need access to Hilt-managed dependencies.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuickPreviewUtilEntryPoint {
    fun quickPreviewUtil(): QuickPreviewUtil
}
