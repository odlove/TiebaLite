package com.huanchengfly.tieba.post.di.entrypoints

import com.huanchengfly.tieba.post.emoticon.EmoticonRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface EmoticonRepositoryEntryPoint {
    fun emoticonRepository(): EmoticonRepository
}
