package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.Tintable;
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver;

@SuppressLint("CustomViewStyleable")
public class TintConstraintLayout extends ConstraintLayout implements Tintable, BackgroundTintable {
    private int backgroundTintResId;

    public TintConstraintLayout(@NonNull Context context) {
        this(context, null);
    }

    public TintConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            backgroundTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, 0);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        if (backgroundTintResId != 0) {
            if (getBackground() == null) {
                setBackgroundColor(ThemeColorResolver.colorById(getContext(), backgroundTintResId));
            } else {
                ColorStateList tint = ColorStateList.valueOf(
                    ThemeColorResolver.colorById(getContext(), backgroundTintResId)
                );
                setBackgroundTintList(tint);
            }
        }
    }

    @Override
    public int getBackgroundTintResId() {
        return backgroundTintResId;
    }

    @Override
    public void setBackgroundTintResId(int resId) {
        backgroundTintResId = resId;
        tint();
    }
}
