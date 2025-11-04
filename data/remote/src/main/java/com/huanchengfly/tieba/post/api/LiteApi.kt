package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.core.network.retrofit.NullOnEmptyConverterFactory
import com.huanchengfly.tieba.core.network.retrofit.RetrofitClientFactory
import com.huanchengfly.tieba.core.network.retrofit.RetrofitClients
import com.huanchengfly.tieba.core.network.retrofit.adapter.DeferredCallAdapterFactory
import com.huanchengfly.tieba.core.network.retrofit.converter.gson.GsonConverterFactory
import com.huanchengfly.tieba.core.network.http.Header
import com.huanchengfly.tieba.core.network.retrofit.interceptors.CommonHeaderInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interfaces.LiteApiInterface
import okhttp3.ConnectionPool

object LiteApi {
    private val connectionPool = ConnectionPool()
    private val retrofitClients = RetrofitClients(RetrofitClientFactory())
    private val builderConfig = RetrofitClients.BuilderConfig(
        callAdapterFactories = listOf(DeferredCallAdapterFactory()),
        converterFactories = listOf(
            NullOnEmptyConverterFactory(),
            GsonConverterFactory.create()
        )
    )
    private val clientConfig = RetrofitClients.ClientConfig(
        connectionPool = connectionPool
    )

    val instance: LiteApiInterface by lazy {
        retrofitClients.createService(
            baseUrl = "https://github.com/",
            builderConfig = builderConfig,
            clientConfig = clientConfig,
            extraInterceptors = listOf(
                CommonHeaderInterceptor(
                    Header.USER_AGENT to { System.getProperty("http.agent") },
                )
            )
        )
    }
}
