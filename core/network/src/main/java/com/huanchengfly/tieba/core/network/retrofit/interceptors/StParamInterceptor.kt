package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.http.Header
import com.huanchengfly.tieba.core.network.http.Method
import com.huanchengfly.tieba.core.network.http.addAllEncoded
import com.huanchengfly.tieba.core.network.http.forEachNonNull
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt

class StParamInterceptor(private val method: Boolean = false) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var headers = request.headers
        var httpUrl = request.url
        var body = request.body

        var addStParam = true
        val noStParams = headers[Header.NO_ST_PARAMS]
        if (noStParams != null) {
            headers = headers.newBuilder().removeAll(Header.NO_ST_PARAMS).build()
            addStParam = noStParams != Header.NO_ST_PARAMS_TRUE
        }

        if (!addStParam) {
            return chain.proceed(request.newBuilder().headers(headers).build())
        }

        var forceQuery = false
        val forceParam = headers[Header.FORCE_PARAM]
        if (forceParam != null) {
            if (forceParam == Header.FORCE_PARAM_QUERY) forceQuery = true
            headers = headers.newBuilder().removeAll(Header.FORCE_PARAM).build()
        }

        val num = ThreadLocalRandom.current().nextInt(100, 850)
        var stErrorNums = "0"
        var stMethod: String? = null
        var stMode: String? = null
        var stTimesNum: String? = null
        var stTime: String? = null
        var stSize: String? = null
        if (num !in 100..120) {
            stErrorNums = "1"
            stMethod = if (method) "2" else "1"
            stMode = "1"
            stTimesNum = "1"
            stTime = num.toString()
            stSize = ((Math.random() * 8 + 0.4) * num).roundToInt().toString()
        }

        val additionParams = arrayOf(
            "stErrorNums" to { stErrorNums },
            "stMethod" to { stMethod },
            "stMode" to { stMode },
            "stTimesNum" to { stTimesNum },
            "stTime" to { stTime },
            "stSize" to { stSize }
        )

        when {
            request.method == Method.GET || forceQuery -> {
                httpUrl = request.url.newBuilder().apply {
                    additionParams.forEachNonNull { name, value ->
                        addQueryParameter(name, value)
                    }
                }.build()
            }

            body == null || body.contentLength() == 0L -> {
                body = FormBody.Builder().apply {
                    additionParams.forEachNonNull { name, value ->
                        add(name, value)
                    }
                }.build()
            }

            body is FormBody -> {
                body = FormBody.Builder().addAllEncoded(body).apply {
                    additionParams.forEachNonNull { name, value ->
                        add(name, value)
                    }
                }.build()
            }

            else -> {
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
