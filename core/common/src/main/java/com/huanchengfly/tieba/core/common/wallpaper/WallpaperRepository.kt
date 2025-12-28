package com.huanchengfly.tieba.core.common.wallpaper

interface WallpaperRepository {
    suspend fun fetchWallpapers(): Result<List<String>>
}
