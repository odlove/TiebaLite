package com.huanchengfly.tieba.core.runtime.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ClientUtils {
    @Volatile
    private var repository: ClientConfigRepository? = null

    @Volatile
    private var state: ClientConfigState = ClientConfigState(
        clientId = null,
        sampleId = null,
        baiduId = null,
        activeTimestamp = System.currentTimeMillis()
    )

    fun configure(repository: ClientConfigRepository, initialState: ClientConfigState) {
        this.repository = repository
        this.state = initialState
    }

    fun updateState(newState: ClientConfigState) {
        state = newState
    }

    val clientId: String?
        get() = state.clientId

    val sampleId: String?
        get() = state.sampleId

    val baiduId: String?
        get() = state.baiduId

    val activeTimestamp: Long
        get() = state.activeTimestamp

    suspend fun sync() {
        val repo = repository ?: return
        withContext(Dispatchers.IO) {
            repo.sync()
            state = repo.load()
        }
    }

    suspend fun saveBaiduId(id: String) {
        val repo = repository ?: return
        withContext(Dispatchers.IO) {
            repo.updateBaiduId(id)
            state = state.copy(baiduId = id)
        }
    }

    suspend fun setActiveTimestamp() {
        val repo = repository ?: return
        withContext(Dispatchers.IO) {
            repo.touchActiveTimestamp()
            state = state.copy(activeTimestamp = System.currentTimeMillis())
        }
    }

    val isConfigured: Boolean
        get() = repository != null
}
