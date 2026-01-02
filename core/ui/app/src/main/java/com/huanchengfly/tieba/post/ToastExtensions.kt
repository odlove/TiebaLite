package com.huanchengfly.tieba.post

import android.content.Context
import android.widget.Toast

fun Context.toastShort(text: String) {
    runCatching { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}

fun Context.toastShort(resId: Int, vararg args: Any) {
    toastShort(getString(resId, *args))
}
