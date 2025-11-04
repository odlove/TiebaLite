package com.huanchengfly.tieba.post.services.notification

import com.huanchengfly.tieba.core.runtime.service.notification.NotificationCounts
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationFetchCallback
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationFetcher
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.MsgBean
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.HttpException

@Singleton
class AppNotificationFetcher @Inject constructor(
    private val api: ITiebaApi
) : NotificationFetcher {

    private var call: Call<MsgBean>? = null

    override fun fetch(callback: NotificationFetchCallback) {
        val request = api.msg()
        call = request
        request.enqueue(object : Callback<MsgBean> {
            override fun onResponse(call: Call<MsgBean>, response: Response<MsgBean>) {
                if (!response.isSuccessful) {
                    callback.onComplete(Result.failure(HttpException(response)))
                    return
                }
                val body = response.body()
                if (body == null) {
                    callback.onComplete(Result.failure(IllegalStateException("Notification response body is empty")))
                    return
                }
                val replyCount = body.message?.replyMe?.toIntOrNull() ?: 0
                val mentionCount = body.message?.atMe?.toIntOrNull() ?: 0
                callback.onComplete(Result.success(NotificationCounts(replyCount, mentionCount)))
            }

            override fun onFailure(call: Call<MsgBean>, t: Throwable) {
                if (!call.isCanceled) {
                    callback.onComplete(Result.failure(t))
                }
            }
        })
    }

    override fun cancel() {
        call?.cancel()
        call = null
    }
}
