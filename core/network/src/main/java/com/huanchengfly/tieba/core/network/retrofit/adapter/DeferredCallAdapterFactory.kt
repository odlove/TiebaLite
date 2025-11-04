package com.huanchengfly.tieba.core.network.retrofit.adapter

import com.huanchengfly.tieba.core.network.retrofit.ApiResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

class DeferredCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Deferred::class.java) {
            return null
        }
        require(returnType is ParameterizedType) { "Call return type must be parameterized as Call<Foo> or Call<? extends Foo>" }
        val responseType = getParameterUpperBound(0, returnType)
        val rawDeferredType = getRawType(responseType)
        return if (rawDeferredType == ApiResult::class.java) {
            require(responseType is ParameterizedType) { "ApiResult must be parameterized as ApiResult<Foo> or ApiResult<out Foo>" }
            ApiResultCallAdapter<Any>(getParameterUpperBound(0, responseType))
        } else {
            BodyCallAdapter<Any>(responseType)
        }
    }

    class ApiResultCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<T, Any> {
        override fun responseType(): Type = responseType

        override fun adapt(call: Call<T>): Any {
            val deferred = CompletableDeferred<ApiResult<T>>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    deferred.complete(ApiResult.Failure(t))
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        deferred.complete(ApiResult.Success(body))
                    } else {
                        deferred.complete(ApiResult.Failure(HttpException(response)))
                    }
                }
            })

            return deferred
        }
    }

    class BodyCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<T, Any> {
        override fun responseType(): Type = responseType

        override fun adapt(call: Call<T>): Any {
            val deferred = CompletableDeferred<T>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    deferred.completeExceptionally(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        deferred.complete(body)
                    } else {
                        deferred.completeExceptionally(HttpException(response))
                    }
                }
            })

            return deferred
        }
    }

    companion object {
        @JvmStatic
        @JvmName("create")
        operator fun invoke() = DeferredCallAdapterFactory()

        fun getParameterUpperBound(index: Int, type: ParameterizedType): Type {
            val types = type.actualTypeArguments
            require(!(index < 0 || index >= types.size)) { "Index $index not in range [0,${types.size}) for $type" }
            val paramType = types[index]
            return if (paramType is WildcardType) {
                paramType.upperBounds[0]
            } else paramType
        }
    }
}
