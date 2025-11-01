package com.huanchengfly.tieba.core.common

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * 提供访问字符串/复数字符串等资源的抽象，便于测试和依赖注入。
 */
interface ResourceProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg args: Any): String
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg args: Any): String
}
