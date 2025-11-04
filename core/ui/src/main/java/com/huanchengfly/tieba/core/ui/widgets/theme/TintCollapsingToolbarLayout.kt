package com.huanchengfly.tieba.core.ui.widgets.theme

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils
import com.huanchengfly.tieba.core.ui.theme.Tintable

class TintCollapsingToolbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CollapsingToolbarLayout(context, attrs, defStyleAttr), Tintable {
    private var textColorResId: Int

    init {
        val array = context.obtainStyledAttributes(
            attrs,
            R.styleable.TintCollapsingToolbarLayout,
            defStyleAttr,
            0
        )
        textColorResId = array.getResourceId(R.styleable.TintCollapsingToolbarLayout_textColor, 0)
        array.recycle()
        tint()
    }

    override fun tint() {
        if (textColorResId == 0) return

        val colorStateList = ColorStateListUtils.getColorStateList(context, textColorResId, isInEditMode)

        if (colorStateList != null) {
            setCollapsedTitleTextColor(colorStateList)
            setExpandedTitleTextColor(colorStateList)
        }
    }
}
