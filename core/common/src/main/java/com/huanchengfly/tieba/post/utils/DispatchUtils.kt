package com.huanchengfly.tieba.post.utils

import android.content.Context

val officialClientPackages = arrayOf("com.baidu.tieba", "com.baidu.tieba_mini")

fun isOfficialClientInstalled(context: Context): Boolean {
    return context.isAnyPackageInstalled(officialClientPackages)
}
