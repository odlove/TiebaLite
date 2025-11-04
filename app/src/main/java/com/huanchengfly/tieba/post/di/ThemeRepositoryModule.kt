package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.ui.theme.data.ThemeRepository
import com.huanchengfly.tieba.core.ui.theme.data.ThemeRepositoryImpl
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
