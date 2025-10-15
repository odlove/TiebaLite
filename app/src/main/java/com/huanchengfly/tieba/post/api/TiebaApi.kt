package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.interfaces.impls.MixedTiebaApiImpl

/**
 * TiebaApi 工具类
 *
 * 提供 ITiebaApi 实例的静态访问方法
 * 注：依赖注入请使用 ApiModule 提供的 ITiebaApi
 */
object TiebaApi {

    @JvmStatic
    fun getInstance(): ITiebaApi = MixedTiebaApiImpl
}