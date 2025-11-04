package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.appbar.AppBarLayout;
import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.Tintable;
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver;

@SuppressLint("CustomViewStyleable")
public class TintAppBarLayout extends AppBarLayout implements Tintable {
    private int backgroundTintResId;

    public TintAppBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public TintAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            backgroundTintResId = R.color.default_color_background;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.default_color_background);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        if (getBackground() == null) {
            setBackgroundColor(ThemeColorResolver.colorById(getContext(), backgroundTintResId));
        } else {
            setBackgroundTintList(ColorStateList.valueOf(ThemeColorResolver.colorById(getContext(), backgroundTintResId)));
        }
    }
}
