package com.huanchengfly.tieba.post.identity

import android.content.Context
import androidx.core.content.edit
import com.huanchengfly.tieba.core.runtime.identity.UuidStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUuidStorage @Inject constructor(
    @ApplicationContext private val context: Context
) : UuidStorage {

    companion object {
        private const val PREF_NAME = "appData"
        private const val KEY_UUID = "uuid"
    }

    override fun getOrCreateUuid(): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var uuid = prefs.getString(KEY_UUID, null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_UUID, uuid) }
        }
        return uuid
    }
}
