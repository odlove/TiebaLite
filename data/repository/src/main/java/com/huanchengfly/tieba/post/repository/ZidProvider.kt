package com.huanchengfly.tieba.post.repository

import kotlinx.coroutines.flow.Flow

interface ZidProvider {
    fun fetchZid(): Flow<String>
}
