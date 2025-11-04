package com.huanchengfly.tieba.core.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils
import com.huanchengfly.tieba.core.ui.theme.Tintable

@SuppressLint("CustomViewStyleable")
class TintTextView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(requireNotNull(context) { "Context cannot be null" }, attrs, defStyleAttr),
    Tintable {

    var backgroundTintResId: Int = 0
        set(value) {
            field = value
            applyTintColor()
        }

    var tintResId: Int = 0
        set(value) {
            field = value
            applyTintColor()
        }

    init {
        val array =
            getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
        backgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, 0)
        tintResId = array.getResourceId(R.styleable.TintView_tint, 0)
        array.recycle()
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
        if (tintResId != 0) {
            val colorStateList = ColorStateListUtils.getColorStateList(context, tintResId, isInEditMode)
            if (colorStateList != null) {
                setTextColor(colorStateList)
            }
        }
    }
}
