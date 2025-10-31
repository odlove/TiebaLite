package com.huanchengfly.tieba.core.network.retrofit.interceptors

import com.huanchengfly.tieba.core.network.http.Param
import com.huanchengfly.tieba.core.network.http.contains
import com.huanchengfly.tieba.core.network.http.containsEncodedName
import com.huanchengfly.tieba.core.network.http.fileName
import com.huanchengfly.tieba.core.network.http.multipart.MyMultipartBody
import com.huanchengfly.tieba.core.network.http.name
import com.huanchengfly.tieba.core.network.http.newBuilder
import com.huanchengfly.tieba.core.network.http.readString
import com.huanchengfly.tieba.core.network.http.sortedEncodedRaw
import com.huanchengfly.tieba.core.network.http.sortedRaw
import com.huanchengfly.tieba.core.network.runtime.SignSecretProvider
import com.huanchengfly.tieba.core.network.runtime.SignSecretRegistry
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

object SortAndSignInterceptor : Interceptor {
    fun registerAppSecretProvider(provider: () -> String) {
        SignSecretRegistry.register(object : SignSecretProvider {
            override val appSecret: String
                get() = provider()
        })
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url
        val body = request.body
        val appSecret = SignSecretRegistry.current.appSecret

        request = when {
            url.queryParameter("BDUSS") != null && url.queryParameter(Param.SIGN) == null -> {
                val query = url.query
                val encodedQuery = url.encodedQuery
                if (query != null && encodedQuery != null) {
                    val sortedQuery = query.split('&').sorted().joinToString(separator = "")
                    val sortedEncodedQuery = encodedQuery.split('&').sorted().joinToString(separator = "&")
                    request.newBuilder()
                        .url(
                            url.newBuilder()
                                .encodedQuery(
                                    "$sortedEncodedQuery&${Param.SIGN}=${calculateSign(sortedQuery, appSecret)}"
                                )
                                .build()
                        ).build()
                } else {
                    request
                }
            }

            body is FormBody &&
                    body.containsEncodedName(Param.CLIENT_VERSION) &&
                    !body.containsEncodedName(Param.SIGN) -> {
                val sortedEncodedRaw = body.sortedEncodedRaw()
                val formBody = FormBody.Builder().apply {
                    sortedEncodedRaw.split('&').forEach {
                        val (name, value) = it.split('=')
                        addEncoded(name, value)
                    }
                    addEncoded(Param.SIGN, calculateSign(body.sortedRaw(false), appSecret))
                }.build()
                request.newBuilder()
                    .method(request.method, formBody)
                    .build()
            }

            body is MyMultipartBody && body.contains(Param.CLIENT_VERSION) && !body.contains(Param.SIGN) -> {
                val builder = body.newBuilder()
                val fileParts = mutableListOf<MyMultipartBody.Part>()
                body.parts.forEach {
                    if (it.fileName() != null) {
                        fileParts.add(it)
                    }
                }
                body.parts.filterNot { it in fileParts }.sortedBy { it.name() }
                    .forEach { builder.addPart(it) }
                var newBody = builder.build()
                val sortedRaw = newBody.parts.filter { it.fileName() == null }
                    .joinToString(separator = "") { "${it.name()}=${it.readString()}" }
                builder.addFormDataPart(Param.SIGN, calculateSign(sortedRaw, appSecret))
                if (fileParts.isNotEmpty()) fileParts.sortedBy { it.fileName() }
                    .forEach { builder.addPart(it) }
                newBody = builder.build()
                request.newBuilder()
                    .method(request.method, newBody)
                    .build()
            }

            else -> {
                request
            }
        }

        return chain.proceed(request)
    }

    internal fun calculateSign(sortedQuery: String, appSecret: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest((sortedQuery + appSecret).toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { "%02x".format(it) }.uppercase()
    }
}
