package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.core.network.exception.TiebaException
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.dpToPxFloat
import com.huanchengfly.tieba.post.getBoolean
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.utils.Util.createSnackbar

@JvmOverloads
fun getItemBackgroundDrawable(
    context: Context,
    position: Int,
    itemCount: Int,
    positionOffset: Int = 0,
    radius: Float = 8f.dpToPxFloat(),
    colors: IntArray = intArrayOf(R.color.sem_surface_card),
    ripple: Boolean = true
): Drawable {
    val realPos = position + positionOffset
    val maxPos = itemCount - 1 + positionOffset
    val shape = GradientDrawable().apply {
        color = ColorStateListUtils.createColorStateList(context, colors[realPos % colors.size])
        if (realPos == 0 && realPos == maxPos) {
            cornerRadius = radius
        } else if (realPos == 0) {
            cornerRadii = floatArrayOf(
                radius,
                radius,
                radius,
                radius,
                0f, 0f, 0f, 0f
            )
        } else if (realPos == maxPos) {
            cornerRadii = floatArrayOf(
                0f, 0f, 0f, 0f,
                radius,
                radius,
                radius,
                radius
            )
        } else {
            cornerRadius = 0f
        }
    }
    return if (ripple) {
        wrapRipple(
            ThemeColorResolver.rippleColor(context),
            shape
        )
    } else {
        shape
    }
}

fun getRadiusDrawable(
    context: Context,
    topLeftPx: Float = 0f,
    topRightPx: Float = 0f,
    bottomLeftPx: Float = 0f,
    bottomRightPx: Float = 0f,
    ripple: Boolean = false
): Drawable {
    val drawable = GradientDrawable().apply {
        color = ColorStateList.valueOf(Color.WHITE)
        cornerRadii = floatArrayOf(
            topLeftPx, topLeftPx,
            topRightPx, topRightPx,
            bottomRightPx, bottomRightPx,
            bottomLeftPx, bottomLeftPx
        )
    }
    return if (ripple)
        wrapRipple(
            ThemeColorResolver.rippleColor(context),
            drawable
        )
    else drawable
}

fun getRadiusDrawable(
    context: Context,
    radiusPx: Float = 0f,
    ripple: Boolean = false
): Drawable {
    return getRadiusDrawable(context, radiusPx, radiusPx, radiusPx, radiusPx, ripple)
}

fun wrapRipple(rippleColor: Int, drawable: Drawable): Drawable {
    return RippleDrawable(ColorStateList.valueOf(rippleColor), drawable, drawable)
}


@JvmOverloads
fun getIntermixedColorBackground(
    context: Context,
    position: Int,
    itemCount: Int,
    positionOffset: Int = 0,
    radius: Float = 8f.dpToPxFloat(),
    colors: IntArray = intArrayOf(R.color.sem_surface_card),
    ripple: Boolean = true
): Drawable {
    return getItemBackgroundDrawable(
        context,
        position,
        itemCount,
        positionOffset,
        radius,
        if (context.appPreferences.listItemsBackgroundIntermixed) {
            colors
        } else {
            intArrayOf(colors[0])
        },
        ripple
    )
}

fun showErrorSnackBar(view: View, throwable: Throwable) {
    val code = if (throwable is TiebaException) throwable.code else -1
    createSnackbar(
        view,
        view.context.getString(R.string.snackbar_error, code),
        Snackbar.LENGTH_LONG
    )
        .setAction(R.string.button_detail) {
            val stackTrace = throwable.stackTraceToString()
            DialogUtil.build(view.context)
                .setTitle(R.string.title_dialog_error_detail)
                .setMessage(stackTrace)
                .setPositiveButton(R.string.button_copy_detail) { _, _ ->
                    TiebaUtil.copyText(view.context, stackTrace)
                }
                .setNegativeButton(R.string.btn_close, null)
                .show()
        }
        .show()
}

fun calcStatusBarColorInt(context: Context, @ColorInt originColor: Int): Int {
    var darkerStatusBar = true
    val isToolbarPrimaryColor =
        context.dataStore.getBoolean(ThemeTokens.KEY_CUSTOM_TOOLBAR_PRIMARY_COLOR, false)
    val themeState = ThemeColorResolver.state(context)
    if (!themeState.isTranslucent && !themeState.isNightMode && !isToolbarPrimaryColor) {
        darkerStatusBar = false
    } else if (!context.dataStore.getBoolean("status_bar_darker", true)) {
        darkerStatusBar = false
    }
    return if (darkerStatusBar) ColorUtils.getDarkerColor(originColor) else originColor
}
