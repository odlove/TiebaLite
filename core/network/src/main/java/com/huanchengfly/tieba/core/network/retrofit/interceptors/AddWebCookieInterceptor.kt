package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.account.AccountTokenRegistry
import com.huanchengfly.tieba.core.network.http.Header
import okhttp3.Interceptor
import okhttp3.Response

object AddWebCookieInterceptor : Interceptor {
    private var cookieProvider: () -> String? = { AccountTokenRegistry.current.cookie }

    fun registerCookieProvider(provider: () -> String?) {
        cookieProvider = provider
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var headers = request.headers
        val httpUrl = request.url
        val body = request.body

        var addCookie = true
        val addCookieHeader = headers[Header.ADD_WEB_COOKIE]
        if (addCookieHeader != null) {
            if (addCookieHeader == Header.ADD_WEB_COOKIE_FALSE) addCookie = false
            headers = headers.newBuilder()
                .removeAll(Header.ADD_WEB_COOKIE)
                .build()
        }

        if (addCookie) {
            val existingValues = headers.values(Header.COOKIE)
            val accountCookie = cookieProvider()?.takeUnless { it.isNullOrBlank() }

            val cookieEntries = linkedSetOf<String>()
            (existingValues + listOfNotNull(accountCookie)).forEach { headerValue ->
                headerValue.split(';')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { cookieEntries.add(it) }
            }

            headers = headers.newBuilder()
                .removeAll(Header.COOKIE)
                .apply {
                    if (cookieEntries.isNotEmpty()) {
                        add(Header.COOKIE, cookieEntries.joinToString(separator = "; "))
                    }
                }
                .build()
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
