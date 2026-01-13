package com.huanchengfly.tieba.core.theme2.di

import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.core.theme2.runtime.ThemeRuntime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object ThemeRuntimeModule {
    @Provides
    @Singleton
    fun provideThemeRuntime(
        @ApplicationScope scope: CoroutineScope
    ): ThemeRuntime = ThemeRuntime(scope)
}
