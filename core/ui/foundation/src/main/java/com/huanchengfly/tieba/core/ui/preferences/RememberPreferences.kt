package com.huanchengfly.tieba.core.ui.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val LocalPreferencesDataStore = staticCompositionLocalOf<DataStore<Preferences>> {
    error("Preferences DataStore 未提供")
}

@Composable
fun <T> rememberPreferenceAsMutableState(
    key: Preferences.Key<T>,
    defaultValue: T,
    dataStore: DataStore<Preferences>? = null
): MutableState<T> {
    val resolvedDataStore = dataStore ?: LocalPreferencesDataStore.current
    val state = remember(resolvedDataStore, key, defaultValue) { mutableStateOf(defaultValue) }
    val isLoaded = remember(resolvedDataStore, key, defaultValue) { mutableStateOf(false) }
    val lastCommitted = remember(resolvedDataStore, key, defaultValue) { mutableStateOf(defaultValue) }
    val pendingValue = remember(resolvedDataStore, key, defaultValue) { mutableStateOf(defaultValue) }
    val hasPending = remember(resolvedDataStore, key, defaultValue) { mutableStateOf(false) }
    val isInitialState = remember(resolvedDataStore, key, defaultValue) { mutableStateOf(true) }

    LaunchedEffect(resolvedDataStore, key, defaultValue) {
        resolvedDataStore.data
            .map { it[key] ?: defaultValue }
            .distinctUntilChanged()
            .collect { value ->
                if (!isLoaded.value) {
                    isLoaded.value = true
                    if (!hasPending.value) {
                        if (state.value != value) {
                            state.value = value
                        }
                        lastCommitted.value = value
                    }
                }

                if (!hasPending.value) {
                    if (value != lastCommitted.value) {
                        lastCommitted.value = value
                    }
                    if (state.value != value) {
                        state.value = value
                    }
                } else if (value == pendingValue.value) {
                    lastCommitted.value = value
                    hasPending.value = false
                }
            }
    }

    LaunchedEffect(resolvedDataStore, key, defaultValue, state.value) {
        if (!isLoaded.value) {
            if (isInitialState.value) {
                isInitialState.value = false
            } else {
                pendingValue.value = state.value
                hasPending.value = true
            }
        } else if (state.value != lastCommitted.value) {
            pendingValue.value = state.value
            hasPending.value = true
        }
    }

    LaunchedEffect(resolvedDataStore, key, defaultValue, isLoaded.value, hasPending.value) {
        if (isLoaded.value && hasPending.value) {
            val pending = pendingValue.value
            if (state.value != pending) {
                state.value = pending
            }
            if (pending != lastCommitted.value) {
                resolvedDataStore.edit { prefs -> prefs[key] = pending }
                lastCommitted.value = pending
            }
            hasPending.value = false
        }
    }

    return state
}

@Composable
fun <T> rememberPreferenceAsState(
    key: Preferences.Key<T>,
    defaultValue: T,
    dataStore: DataStore<Preferences>? = null
): State<T> {
    val resolvedDataStore = dataStore ?: LocalPreferencesDataStore.current
    val state = remember(resolvedDataStore, key, defaultValue) { mutableStateOf(defaultValue) }

    LaunchedEffect(resolvedDataStore, key, defaultValue) {
        resolvedDataStore.data
            .map { it[key] ?: defaultValue }
            .distinctUntilChanged()
            .collect { value ->
                if (state.value != value) {
                    state.value = value
                }
            }
    }

    return state
}

@Composable
fun <T> DataStore<Preferences>.collectPreferenceAsState(
    key: Preferences.Key<T>,
    defaultValue: T
): State<T> {
    return data.map { it[key] ?: defaultValue }.collectAsState(initial = defaultValue)
}
