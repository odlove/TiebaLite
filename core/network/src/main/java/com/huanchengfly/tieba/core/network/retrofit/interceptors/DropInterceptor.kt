package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.http.Header
import com.huanchengfly.tieba.core.network.http.Method
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response

object DropInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var headers = request.headers
        var httpUrl = request.url
        var body = request.body

        val dropHeadersHeader = headers[Header.DROP_HEADERS]
        if (dropHeadersHeader != null) {
            headers = headers.newBuilder()
                .apply {
                    removeAll(Header.DROP_HEADERS)
                    dropHeadersHeader.split(",").forEach { removeAll(it) }
                }
                .build()
        }

        val dropParamsHeader = headers[Header.DROP_PARAMS]
        if (dropParamsHeader != null) {
            headers = headers.newBuilder().removeAll(Header.DROP_PARAMS).build()
            val dropParams = dropParamsHeader.split(",")
            when {
                request.method == Method.GET -> {
                    httpUrl = request.url.newBuilder().apply {
                        dropParams.forEach { removeAllQueryParameters(it) }
                    }.build()
                }

                body is FormBody -> {
                    val oldBody = body
                    val newBody = FormBody.Builder().apply {
                        for (i in 0 until oldBody.size) {
                            if (!dropParams.contains(oldBody.name(i))) {
                                add(oldBody.name(i), oldBody.value(i))
                            }
                        }
                    }.build()
                    body = newBody
                }
            }
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
