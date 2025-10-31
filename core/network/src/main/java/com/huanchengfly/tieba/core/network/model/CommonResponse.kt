package com.huanchengfly.tieba.core.network.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.core.network.json.ErrorMsgAdapter

data class CommonResponse(
    @SerializedName("error_code", alternate = ["errno", "no"])
    val errorCode: Int = 0,
    @JsonAdapter(ErrorMsgAdapter::class)
    @SerializedName("error_msg", alternate = ["errmsg", "error"])
    val errorMsg: String = ""
)
