package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.wallpaper.WallpaperRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WallpaperRepositoryBindings {
    @Binds
    abstract fun bindWallpaperRepository(
        impl: WallpaperRepositoryImpl
    ): WallpaperRepository
}
