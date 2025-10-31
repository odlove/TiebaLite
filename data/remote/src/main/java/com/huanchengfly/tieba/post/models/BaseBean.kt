package com.huanchengfly.tieba.post.models

import com.huanchengfly.tieba.post.utils.GsonUtil

open class BaseBean {
    override fun toString(): String {
        return GsonUtil.getGson().toJson(this)
    }
}
