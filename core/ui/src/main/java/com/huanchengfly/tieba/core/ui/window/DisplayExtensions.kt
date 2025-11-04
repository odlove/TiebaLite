package com.huanchengfly.tieba.core.ui.window

import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import androidx.annotation.FloatRange
import android.content.Context

/**
 * 将 px 转换为 dp。
 */
fun Context.pxToDp(px: Float): Float = px / resources.displayMetrics.density

/**
 * 将 dp 转换为 px。
 */
fun Context.dpToPx(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

/**
 * 将 px 转换为 sp。
 */
fun Context.pxToSp(px: Float): Float = px / resources.displayMetrics.scaledDensity

/**
 * 将 sp 转换为 px。
 */
fun Context.spToPx(sp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

/**
 * 获取屏幕宽度（像素）。
 */
fun Context.screenWidthPx(): Int = resources.displayMetrics.widthPixels

/**
 * 获取屏幕高度（像素）。
 */
fun Context.screenHeightPx(): Int = resources.displayMetrics.heightPixels

