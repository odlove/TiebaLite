package com.huanchengfly.tieba.core.common.theme.di

import com.huanchengfly.tieba.core.common.theme.ThemeRepository
import com.huanchengfly.tieba.core.common.theme.ThemeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}
