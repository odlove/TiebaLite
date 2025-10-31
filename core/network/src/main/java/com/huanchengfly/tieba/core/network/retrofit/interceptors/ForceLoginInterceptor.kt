package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.account.AccountTokenRegistry
import com.huanchengfly.tieba.core.network.http.Header
import okhttp3.Interceptor
import okhttp3.Response

object ForceLoginInterceptor : Interceptor {
    private var exceptionFactory: () -> Throwable = {
        IllegalStateException("Login required but no handler provided")
    }

    fun registerExceptionFactory(factory: () -> Throwable) {
        exceptionFactory = factory
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var headers = request.headers
        val httpUrl = request.url
        val body = request.body

        var forceLogin = false
        val forceLoginHeader = headers[Header.FORCE_LOGIN]
        if (forceLoginHeader != null) {
            if (forceLoginHeader == Header.FORCE_LOGIN_TRUE) forceLogin = true
            headers = headers.newBuilder().removeAll(Header.FORCE_LOGIN).build()
        }

        if (forceLogin && !AccountTokenRegistry.current.isLoggedIn) {
            throw exceptionFactory.invoke()
        }

        return chain.proceed(
            request.newBuilder()
                .headers(headers)
                .url(httpUrl)
                .method(request.method, body)
                .build()
        )
    }
}
