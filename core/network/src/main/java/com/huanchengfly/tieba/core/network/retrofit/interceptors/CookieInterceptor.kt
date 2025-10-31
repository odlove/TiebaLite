package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.identity.ClientIdentityRegistry
import okhttp3.Interceptor
import okhttp3.Response

object CookieInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val cookies = response.headers("Set-Cookie")
        if (cookies.isNotEmpty()) {
            cookies.forEach {
                val cookieName = it.substringBefore("=")
                val cookieValue = it.substringAfter("=").substringBefore(";")
                if (
                    cookieName.equals("BAIDUID", ignoreCase = true) &&
                    ClientIdentityRegistry.current.baiduId.isNullOrEmpty()
                ) {
                    ClientIdentityRegistry.baiduIdHandler.saveBaiduId(cookieValue)
                }
            }
        }

        return response
    }
}
