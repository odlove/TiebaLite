package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.error.ErrorMessages
import com.huanchengfly.tieba.core.network.exception.NoConnectivityException
import com.huanchengfly.tieba.core.network.runtime.NetworkStatusProvider
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

object ConnectivityInterceptor : Interceptor {
    private var networkStatusProvider: NetworkStatusProvider = NetworkStatusProvider.AlwaysConnected

    fun registerNetworkStatusProvider(provider: NetworkStatusProvider) {
        networkStatusProvider = provider
    }

    private fun isConnected(): Boolean = networkStatusProvider.isConnected()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = runCatching { chain.proceed(chain.request()) }

        val exception = response.exceptionOrNull()
        val connected = isConnected()

        return when {
            (exception is SocketTimeoutException || exception is SocketException || exception is SSLHandshakeException) && connected -> throw NoConnectivityException(
                ErrorMessages.current().networkTimeout()
            )

            exception is IOException && !connected -> throw NoConnectivityException(
                ErrorMessages.current().noConnectivity()
            )

            else -> response.getOrThrow()
        }
    }
}
