package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

open class WebBaseBean<Data> : BaseBean() {
    @SerializedName("no")
    var errorCode: Int = 0

    @SerializedName("error")
    var errorMsg: String? = null

    var data: Data? = null
}
