package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeStyleProvider
import com.huanchengfly.tieba.post.theme.AppThemeStyleProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeStyleProviderModule {
    @Binds
    @Singleton
    abstract fun bindThemeStyleProvider(impl: AppThemeStyleProvider): ThemeStyleProvider
}
