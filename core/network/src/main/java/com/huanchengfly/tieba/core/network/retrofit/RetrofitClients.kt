package com.huanchengfly.tieba.core.network.retrofit

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central entry point for building Retrofit clients with shared defaults.
 *
 * The provider wraps [RetrofitClientFactory] so that downstream modules can
 * obtain consistent OkHttp configuration (timeouts, connection pool, common
 * interceptors) while still being able to register additional interceptors
 * per-service.
 */
@Singleton
class RetrofitClients @Inject constructor(
    private val clientFactory: RetrofitClientFactory
) {

    data class BuilderConfig(
        val callAdapterFactories: List<retrofit2.CallAdapter.Factory> = emptyList(),
        val converterFactories: List<retrofit2.Converter.Factory> = emptyList()
    )

    data class ClientConfig(
        val readTimeoutSec: Long = DEFAULT_TIMEOUT_SEC,
        val connectTimeoutSec: Long = DEFAULT_TIMEOUT_SEC,
        val writeTimeoutSec: Long = DEFAULT_TIMEOUT_SEC,
        val connectionPool: ConnectionPool = DEFAULT_CONNECTION_POOL,
        val interceptors: List<Interceptor> = emptyList()
    ) {
        internal fun toFactoryConfig(): RetrofitClientFactory.OkHttpConfig =
            RetrofitClientFactory.OkHttpConfig(
                readTimeoutSec = readTimeoutSec,
                connectTimeoutSec = connectTimeoutSec,
                writeTimeoutSec = writeTimeoutSec,
                connectionPool = connectionPool,
                interceptors = interceptors
            )
    }

    fun newOkHttpClient(config: ClientConfig): OkHttpClient {
        return clientFactory.createOkHttpClient(config.toFactoryConfig())
    }

    fun newRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        builderConfig: BuilderConfig = BuilderConfig()
    ): Retrofit {
        val builder = Retrofit.Builder()
        builderConfig.callAdapterFactories.forEach { builder.addCallAdapterFactory(it) }
        builderConfig.converterFactories.forEach { builder.addConverterFactory(it) }
        return clientFactory.createRetrofit(baseUrl, okHttpClient, builder)
    }

    inline fun <reified T : Any> createService(
        baseUrl: String,
        builderConfig: BuilderConfig,
        clientConfig: ClientConfig,
        extraInterceptors: List<Interceptor> = emptyList()
    ): T {
        val okHttp = newOkHttpClient(
            clientConfig.copy(interceptors = extraInterceptors + clientConfig.interceptors)
        )
        return newRetrofit(baseUrl, okHttp, builderConfig).create(T::class.java)
    }

    companion object {
        private const val DEFAULT_TIMEOUT_SEC = 60L
        private val DEFAULT_CONNECTION_POOL = ConnectionPool(32, 5, java.util.concurrent.TimeUnit.MINUTES)
    }
}
