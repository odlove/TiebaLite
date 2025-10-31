package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

class NewUpdateBean {
    @SerializedName("success")
    val isSuccess: Boolean = false

    @SerializedName("has_update")
    val isHasUpdate: Boolean = false

    @SerializedName("error_code")
    val errorCode: Int? = null

    @SerializedName("error_message")
    val errorMsg: String? = null
    val result: ResultBean? = null

    class ResultBean {
        val isCancelable: Boolean? = null

        @SerializedName("update_content")
        val updateContent: List<String>? = null

        @SerializedName("version_code")
        val versionCode: Int? = null

        @SerializedName("version_name")
        val versionName: String? = null

        @SerializedName("version_type")
        val versionType: Int? = null
        val downloads: List<DownloadBean>? = null
    }

    class DownloadBean {
        val name: String? = null
        val url: String? = null
    }
}
