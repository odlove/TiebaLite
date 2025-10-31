package com.huanchengfly.tieba.core.network.retrofit

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class RetrofitClientFactory {
    data class OkHttpConfig(
        val readTimeoutSec: Long = 60,
        val connectTimeoutSec: Long = 60,
        val writeTimeoutSec: Long = 60,
        val connectionPool: ConnectionPool = ConnectionPool(32, 5, TimeUnit.MINUTES),
        val interceptors: List<Interceptor> = emptyList()
    )

    fun createOkHttpClient(config: OkHttpConfig, builder: OkHttpClient.Builder = OkHttpClient.Builder()): OkHttpClient {
        return builder.apply {
            readTimeout(config.readTimeoutSec, TimeUnit.SECONDS)
            connectTimeout(config.connectTimeoutSec, TimeUnit.SECONDS)
            writeTimeout(config.writeTimeoutSec, TimeUnit.SECONDS)
            connectionPool(config.connectionPool)
            config.interceptors.forEach { addInterceptor(it) }
        }.build()
    }

    fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        builder: Retrofit.Builder
    ): Retrofit {
        return builder
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
    }
}
