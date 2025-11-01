package com.huanchengfly.tieba.post.data.client

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.core.runtime.client.ClientConfigRepository
import com.huanchengfly.tieba.core.runtime.client.ClientConfigState
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ITiebaApi
) : ClientConfigRepository {

    private val clientIdKey = stringPreferencesKey("client_id")
    private val sampleIdKey = stringPreferencesKey("sample_id")
    private val baiduIdKey = stringPreferencesKey("baidu_id")
    private val activeTimestampKey = longPreferencesKey("active_timestamp")

    private var cachedState: ClientConfigState = ClientConfigState(null, null, null, System.currentTimeMillis())

    override suspend fun load(): ClientConfigState = withContext(Dispatchers.IO) {
        val prefs = context.dataStore.data.firstOrNull()
        cachedState = ClientConfigState(
            clientId = prefs?.get(clientIdKey)?.takeIf { it.isNotEmpty() },
            sampleId = prefs?.get(sampleIdKey)?.takeIf { it.isNotEmpty() },
            baiduId = prefs?.get(baiduIdKey)?.takeIf { it.isNotEmpty() },
            activeTimestamp = prefs?.get(activeTimestampKey) ?: System.currentTimeMillis()
        )
        cachedState
    }

    override suspend fun sync() {
        api.syncFlow(cachedState.clientId)
            .collect { response ->
                val newState = cachedState.copy(
                    clientId = response.client.clientId,
                    sampleId = response.wlConfig.sampleId
                )
                cachedState = newState
                context.updatePreferences {
                    it[clientIdKey] = newState.clientId.orEmpty()
                    it[sampleIdKey] = newState.sampleId.orEmpty()
                }
            }
    }

    override suspend fun updateBaiduId(id: String) {
        cachedState = cachedState.copy(baiduId = id)
        context.updatePreferences { it[baiduIdKey] = id }
    }

    override suspend fun touchActiveTimestamp() {
        val timestamp = System.currentTimeMillis()
        cachedState = cachedState.copy(activeTimestamp = timestamp)
        context.updatePreferences { it[activeTimestampKey] = timestamp }
    }

    private suspend fun Context.updatePreferences(block: suspend (MutablePreferences) -> Unit) {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                block(prefs)
            }
        }
    }
}
