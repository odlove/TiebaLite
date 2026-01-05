package com.huanchengfly.tieba.core.ui.view.tint;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.DialogTitle;

import com.huanchengfly.tieba.core.ui.view.R;
import com.huanchengfly.tieba.core.ui.view.tint.ColorStateListUtils;
import com.huanchengfly.tieba.core.ui.view.tint.Tintable;
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeColorResolver;
import com.huanchengfly.tieba.core.ui.view.runtime.bridge.ThemeDrawableUtils;

@SuppressLint({"CustomViewStyleable", "RestrictedApi"})
public class TintDialogTitle extends DialogTitle implements Tintable {
    private int backgroundTintResId;
    private int tintResId;
    private int tintListResId;

    public TintDialogTitle(Context context) {
        this(context, null);
    }

    public TintDialogTitle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintDialogTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs == null) {
            backgroundTintResId = R.color.transparent;
            tintResId = 0;
            tintListResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.transparent);
        tintResId = array.getResourceId(R.styleable.TintView_tint, 0);
        tintListResId = array.getResourceId(R.styleable.TintView_tintList, 0);
        array.recycle();
        applyTintColor();
    }

    public void setBackgroundTintResId(int resId) {
        backgroundTintResId = resId;
        applyTintColor();
    }

    public void setTintResId(int resId) {
        tintResId = resId;
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        int backgroundColor = ColorStateListUtils.resolveColor(getContext(), backgroundTintResId, isInEditMode());
        if (getBackground() == null) {
            setBackgroundColor(backgroundColor);
        } else {
            setBackground(ThemeDrawableUtils.tint(getBackground(), backgroundColor));
        }
        if (tintResId != 0 && tintListResId == 0) {
            setTextColor(ColorStateList.valueOf(ColorStateListUtils.resolveColor(getContext(), tintResId, isInEditMode())));
        } else if (tintListResId != 0) {
            setTextColor(ColorStateListUtils.getColorStateList(getContext(), tintListResId, isInEditMode()));
        }
    }
}
