package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.http.Header
import com.huanchengfly.tieba.core.network.http.Method
import com.huanchengfly.tieba.core.network.http.ParamExpression
import com.huanchengfly.tieba.core.network.http.addAllEncoded
import com.huanchengfly.tieba.core.network.http.addAllParts
import com.huanchengfly.tieba.core.network.http.contains
import com.huanchengfly.tieba.core.network.http.containsEncodedName
import com.huanchengfly.tieba.core.network.http.forEachNonNull
import com.huanchengfly.tieba.core.network.http.multipart.MyMultipartBody
import com.huanchengfly.tieba.core.network.http.newBuilder
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response

class CommonParamInterceptor(private val additionParams: List<ParamExpression>) : Interceptor {
    constructor(vararg additionParams: ParamExpression) : this(additionParams.toList())

    operator fun plus(interceptor: CommonParamInterceptor): CommonParamInterceptor {
        return CommonParamInterceptor(additionParams + interceptor.additionParams)
    }

    operator fun minus(name: String): CommonParamInterceptor {
        return CommonParamInterceptor(additionParams.filter { it.first != name })
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var headers = request.headers
        var httpUrl = request.url
        var body = request.body

        var forceQuery = false
        val forceParam = headers[Header.FORCE_PARAM]
        if (forceParam != null) {
            if (forceParam == Header.FORCE_PARAM_QUERY) forceQuery = true
            headers = headers.newBuilder().removeAll(Header.FORCE_PARAM).build()
        }

        val noCommonParams = mutableListOf<String>()
        val noCommonParamsHeader = headers[Header.NO_COMMON_PARAMS]
        if (noCommonParamsHeader != null) {
            noCommonParams.addAll(noCommonParamsHeader.split(","))
            headers = headers.newBuilder().removeAll(Header.NO_COMMON_PARAMS).build()
        }

        when {
            request.method == Method.GET || forceQuery -> {
                httpUrl = request.url.newBuilder().apply {
                    additionParams.forEachNonNull { name, value ->
                        if (request.url.queryParameter(name) == null &&
                            !noCommonParams.contains(name)
                        ) {
                            addQueryParameter(name, value)
                        }
                    }
                }.build()
            }

            body == null || body.contentLength() == 0L -> {
                body = FormBody.Builder().apply {
                    additionParams.forEachNonNull { name, value ->
                        if (!noCommonParams.contains(name)) {
                            add(name, value)
                        }
                    }
                }.build()
            }

            body is FormBody -> {
                body = FormBody.Builder().addAllEncoded(body).apply {
                    additionParams.forEachNonNull { name, value ->
                        if (!(request.body as FormBody).containsEncodedName(name) &&
                            !noCommonParams.contains(name)
                        ) {
                            add(name, value)
                        }
                    }
                }.build()
            }

            body is MyMultipartBody -> {
                val oldBody = body
                body = oldBody.newBuilder()
                    .addAllParts(oldBody).apply {
                        additionParams.forEachNonNull { name, value ->
                            if (!oldBody.contains(name) &&
                                !noCommonParams.contains(name)
                            ) {
                                addFormDataPart(name, value)
                            }
                        }
                    }.build()
            }

            else -> {}
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
