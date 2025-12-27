package com.huanchengfly.tieba.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.huanchengfly.tieba.data.local.proto.ThemeSettings

val Context.themeSettingsDataStore: DataStore<ThemeSettings> by dataStore(
    fileName = "theme_settings.pb",
    serializer = ThemeSettingsSerializer
)
