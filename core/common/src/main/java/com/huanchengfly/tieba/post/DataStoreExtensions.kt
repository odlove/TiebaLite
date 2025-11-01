package com.huanchengfly.tieba.post

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object DataStoreConst {
    const val DATA_STORE_NAME = "app_preferences"
}

private val dataStoreDelegate = preferencesDataStore(
        name = DataStoreConst.DATA_STORE_NAME,
        produceMigrations = { context ->
            listOf(
                androidx.datastore.preferences.SharedPreferencesMigration(context, "settings"),
                object : DataMigration<Preferences> {
                    override suspend fun cleanUp() = Unit

                    override suspend fun migrate(currentData: Preferences): Preferences {
                        val mutable = currentData.toMutablePreferences()
                        mutable[stringPreferencesKey("dark_theme")] = "grey_dark"
                        return mutable.toPreferences()
                    }

                    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                        return currentData[stringPreferencesKey("dark_theme")] == "dark"
                    }
                }
            )
        }
    )

val Context.dataStore: DataStore<Preferences> by dataStoreDelegate

@SuppressLint("MissingPermission")
fun DataStore<Preferences>.putString(key: String, value: String? = null) {
    MainScope().launch(Dispatchers.IO) {
        edit { prefs ->
            if (value == null) {
                prefs.remove(stringPreferencesKey(key))
            } else {
                prefs[stringPreferencesKey(key)] = value
            }
        }
    }
}

fun DataStore<Preferences>.putBoolean(key: String, value: Boolean) {
    MainScope().launch(Dispatchers.IO) {
        edit { prefs ->
            prefs[booleanPreferencesKey(key)] = value
        }
    }
}

fun DataStore<Preferences>.putInt(key: String, value: Int) {
    MainScope().launch(Dispatchers.IO) {
        edit { prefs ->
            prefs[intPreferencesKey(key)] = value
        }
    }
}

fun DataStore<Preferences>.putLong(key: String, value: Long) {
    MainScope().launch(Dispatchers.IO) {
        edit { prefs ->
            prefs[longPreferencesKey(key)] = value
        }
    }
}

fun DataStore<Preferences>.putFloat(key: String, value: Float) {
    MainScope().launch(Dispatchers.IO) {
        edit { prefs ->
            prefs[floatPreferencesKey(key)] = value
        }
    }
}

fun DataStore<Preferences>.putStringSet(key: String, values: Set<String>? = null) {
    MainScope().launch(Dispatchers.IO) {
        edit { prefs ->
            if (values == null) {
                prefs.remove(stringSetPreferencesKey(key))
            } else {
                prefs[stringSetPreferencesKey(key)] = values.toSet()
            }
        }
    }
}

fun DataStore<Preferences>.getInt(key: String, defaultValue: Int): Int {
    var resultValue = defaultValue
    runBlocking {
        data.first {
            resultValue = it[intPreferencesKey(key)] ?: resultValue
            true
        }
    }
    return resultValue
}

fun DataStore<Preferences>.getString(key: String): String? {
    var resultValue: String? = null
    runBlocking {
        data.first {
            resultValue = it[stringPreferencesKey(key)]
            true
        }
    }
    return resultValue
}

fun DataStore<Preferences>.getString(key: String, defaultValue: String): String {
    var resultValue = defaultValue
    runBlocking {
        data.first {
            resultValue = it[stringPreferencesKey(key)] ?: resultValue
            true
        }
    }
    return resultValue
}

fun DataStore<Preferences>.getStringSet(
    key: String,
    defaultValues: MutableSet<String>? = null
): MutableSet<String>? {
    var resultValue = defaultValues
    runBlocking {
        data.first {
            resultValue = it[stringSetPreferencesKey(key)]?.toMutableSet() ?: resultValue
            true
        }
    }
    return resultValue
}

fun DataStore<Preferences>.getBoolean(key: String, defaultValue: Boolean): Boolean {
    var resultValue = defaultValue
    runBlocking {
        data.first {
            resultValue = it[booleanPreferencesKey(key)] ?: resultValue
            true
        }
    }
    return resultValue
}

fun DataStore<Preferences>.getFloat(key: String, defaultValue: Float): Float {
    var resultValue = defaultValue
    runBlocking {
        data.first {
            resultValue = it[floatPreferencesKey(key)] ?: resultValue
            true
        }
    }
    return resultValue
}

fun DataStore<Preferences>.getLong(key: String, defaultValue: Long): Long {
    var resultValue = defaultValue
    runBlocking {
        data.first {
            resultValue = it[longPreferencesKey(key)] ?: resultValue
            true
        }
    }
    return resultValue
}
