package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.exception.TiebaApiException
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.core.network.proto.ProtoCommonResponse
import okhttp3.Interceptor
import okhttp3.Response

object ProtoFailureResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body
        if (!response.isSuccessful || body == null || body.contentLength() == 0L) return response

        val inputStream = body.source().also {
            it.request(Long.MAX_VALUE)
        }.buffer.clone().inputStream()

        val protoCommonResponse = try {
            ProtoCommonResponse.ADAPTER.decode(inputStream)
        } catch (exception: Exception) {
            return response
        } finally {
            inputStream.close()
        }

        val error = protoCommonResponse.error
        if (error != null && error.error_code != 0) {
            throw TiebaApiException(CommonResponse(error.error_code, error.error_msg))
        }

        return response
    }
}
