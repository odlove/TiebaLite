package com.huanchengfly.tieba.post.data.account

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AccountServiceEntryPoint {
    fun accountService(): AccountService
}
