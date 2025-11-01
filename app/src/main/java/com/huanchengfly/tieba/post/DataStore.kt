package com.huanchengfly.tieba.post

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun <T> rememberPreferenceAsMutableState(
    key: Preferences.Key<T>,
    defaultValue: T
): MutableState<T> {
    val dataStore = LocalContext.current.dataStore
    val state = remember { mutableStateOf(defaultValue) }

    LaunchedEffect(Unit) {
        dataStore.data.map { it[key] ?: defaultValue }.distinctUntilChanged()
            .collect { state.value = it }
    }

    LaunchedEffect(state.value) {
        dataStore.edit { it[key] = state.value }
    }

    return state
}

@Composable
fun <T> rememberPreferenceAsState(
    key: Preferences.Key<T>,
    defaultValue: T
): State<T> {
    val dataStore = LocalContext.current.dataStore
    val state = remember { mutableStateOf(defaultValue) }

    LaunchedEffect(Unit) {
        dataStore.data.map { it[key] ?: defaultValue }.distinctUntilChanged()
            .collect { state.value = it }
    }

    LaunchedEffect(state.value) {
        dataStore.edit { it[key] = state.value }
    }

    return state
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun <T> DataStore<Preferences>.collectPreferenceAsState(
    key: Preferences.Key<T>,
    defaultValue: T
): State<T> {
    return data.map { it[key] ?: defaultValue }.collectAsState(initial = defaultValue)
}

class DataStorePreference : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        App.INSTANCE.dataStore.putString(key, value)
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        App.INSTANCE.dataStore.putStringSet(key, values)
    }

    override fun putInt(key: String, value: Int) {
        App.INSTANCE.dataStore.putInt(key, value)
    }

    override fun putLong(key: String, value: Long) {
        App.INSTANCE.dataStore.putLong(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        App.INSTANCE.dataStore.putFloat(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        App.INSTANCE.dataStore.putBoolean(key, value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return App.INSTANCE.dataStore.getString(key) ?: defValue
    }

    override fun getStringSet(
        key: String,
        defValues: MutableSet<String>?
    ): MutableSet<String>? {
        return App.INSTANCE.dataStore.getStringSet(key, defValues)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return App.INSTANCE.dataStore.getInt(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return App.INSTANCE.dataStore.getLong(key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return App.INSTANCE.dataStore.getFloat(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return App.INSTANCE.dataStore.getBoolean(key, defValue)
    }
}
