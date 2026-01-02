package com.huanchengfly.tieba.core.theme.di

import com.huanchengfly.tieba.core.theme.runtime.controller.ThemeController
import com.huanchengfly.tieba.core.theme.runtime.controller.AppThemeController
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
