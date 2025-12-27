package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.common.preferences.ThemePreferencesDataSource
import com.huanchengfly.tieba.data.local.preferences.ThemePreferencesDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemePreferencesModule {

    @Binds
    @Singleton
    abstract fun bindThemePreferencesDataSource(
        impl: ThemePreferencesDataSourceImpl
    ): ThemePreferencesDataSource
}
