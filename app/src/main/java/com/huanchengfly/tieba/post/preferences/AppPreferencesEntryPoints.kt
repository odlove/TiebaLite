package com.huanchengfly.tieba.post.preferences

import android.content.Context
import com.huanchengfly.tieba.core.common.preferences.AppPreferencesDataSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

val Context.appPreferences: AppPreferencesDataSource
    get() = EntryPointAccessors.fromApplication(
        applicationContext,
        AppPreferencesEntryPoint::class.java
    ).appPreferences()

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppPreferencesEntryPoint {
    fun appPreferences(): AppPreferencesDataSource
}
