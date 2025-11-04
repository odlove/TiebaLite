package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.runtime.AppThemeController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeControllerModule {
    @Binds
    @Singleton
    abstract fun bindThemeController(impl: AppThemeController): ThemeController
}
