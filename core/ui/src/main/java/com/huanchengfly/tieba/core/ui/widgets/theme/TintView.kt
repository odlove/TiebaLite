package com.huanchengfly.tieba.core.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils
import com.huanchengfly.tieba.core.ui.theme.Tintable

@SuppressLint("CustomViewStyleable")
class TintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Tintable {
    private var backgroundTintResId: Int = 0

    init {
        if (attrs != null) {
            val array =
                context.obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
            backgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, 0)
            array.recycle()
        }
        applyTintColor()
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (backgroundTintResId != 0) {
            if (background == null) {
                background = ColorDrawable(Color.WHITE)
            }
            backgroundTintList = ColorStateListUtils.getColorStateList(context, backgroundTintResId, isInEditMode)
        }
    }
}
