package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.google.gson.Gson
import com.huanchengfly.tieba.core.network.exception.TiebaApiException
import com.huanchengfly.tieba.core.network.model.CommonResponse
import okhttp3.Interceptor
import okhttp3.Response

object FailureResponseInterceptor : Interceptor {
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response
        if (!response.isSuccessful || body.contentLength() == 0L) return response

        val contentType = body.contentType()
        val charset = contentType?.charset(Charsets.UTF_8) ?: Charsets.UTF_8

        val source = body.source().also { it.request(Long.MAX_VALUE) }.buffer.clone()
        val commonResponse = source.inputStream().reader(charset).use { reader ->
            runCatching {
                gson.fromJson(reader, CommonResponse::class.java)
            }.getOrNull()
        } ?: return response

        if (commonResponse.errorCode != 0) {
            throw TiebaApiException(commonResponse)
        }

        return response
    }
}
