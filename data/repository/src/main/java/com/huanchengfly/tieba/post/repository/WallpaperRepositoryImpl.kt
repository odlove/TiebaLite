package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.wallpaper.WallpaperRepository
import com.huanchengfly.tieba.core.network.retrofit.ApiResult
import com.huanchengfly.tieba.post.api.LiteApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRepositoryImpl @Inject constructor() : WallpaperRepository {
    override suspend fun fetchWallpapers(): Result<List<String>> {
        return when (val result = LiteApi.instance.wallpapersAsync().await()) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Failure -> Result.failure(result.error)
        }
    }
}
