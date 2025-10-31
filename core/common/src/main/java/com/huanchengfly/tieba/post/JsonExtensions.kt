package com.huanchengfly.tieba.post

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.huanchengfly.tieba.post.utils.GsonUtil
import java.io.File

inline fun <reified Data> String.fromJson(): Data {
    val type = object : TypeToken<Data>() {}.type
    return GsonUtil.getGson().fromJson(this, type)
}

inline fun <reified Data> File.fromJson(): Data {
    val type = object : TypeToken<Data>() {}.type
    return GsonUtil.getGson().fromJson(reader(), type)
}

fun Any.toJson(): String = Gson().toJson(this)
