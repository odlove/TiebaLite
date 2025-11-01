package com.huanchengfly.tieba.post.resources

import android.content.Context
import com.huanchengfly.tieba.core.common.ResourceProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourceProvider {
    override fun getString(resId: Int): String = context.getString(resId)

    override fun getString(resId: Int, vararg args: Any): String =
        context.getString(resId, *args)

    override fun getQuantityString(resId: Int, quantity: Int): String =
        context.resources.getQuantityString(resId, quantity)

    override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String =
        context.resources.getQuantityString(resId, quantity, *args)
}
