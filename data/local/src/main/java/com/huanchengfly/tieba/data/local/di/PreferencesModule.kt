package com.huanchengfly.tieba.data.local.di

import com.huanchengfly.tieba.core.common.preferences.AppPreferencesDataSource
import com.huanchengfly.tieba.data.local.preferences.DataStoreAppPreferencesDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindAppPreferencesDataSource(
        impl: DataStoreAppPreferencesDataSource
    ): AppPreferencesDataSource
}
