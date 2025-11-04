package com.huanchengfly.tieba.core.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.LinearLayout
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils
import com.huanchengfly.tieba.core.ui.theme.Tintable

@SuppressLint("CustomViewStyleable")
class TintLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), Tintable, BackgroundTintable {

    var backgroundTintRes: Int = 0

    init {
        if (attrs != null) {
            val array =
                context.obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
            backgroundTintRes = array.getResourceId(R.styleable.TintView_backgroundTint, 0)
            array.recycle()
        }
        applyTintColor()
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (backgroundTintRes != 0) {
            if (background == null) {
                background = ColorDrawable(Color.WHITE)
            }
            backgroundTintList = ColorStateListUtils.getColorStateList(context, backgroundTintRes, isInEditMode)
        }
    }

    override fun setBackground(background: Drawable) {
        super.setBackground(background)
        applyTintColor()
    }

    override fun setBackgroundTintResId(resId: Int) {
        backgroundTintRes = resId
        tint()
    }

    override fun getBackgroundTintResId(): Int = backgroundTintRes
}
