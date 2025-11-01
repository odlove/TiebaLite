package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.utils.MD5Util

fun String.toMD5(): String = MD5Util.toMd5(this)

fun ByteArray.toMD5(): String = MD5Util.toMd5(this)
