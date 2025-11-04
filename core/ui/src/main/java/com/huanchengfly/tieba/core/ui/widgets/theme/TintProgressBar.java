package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils;
import com.huanchengfly.tieba.core.ui.theme.Tintable;

@SuppressLint("CustomViewStyleable")
public class TintProgressBar extends ContentLoadingProgressBar implements Tintable {
    private int backgroundTintResId;
    private int progressTintResId;
    private int progressBackgroundTintResId;

    public TintProgressBar(@NonNull Context context) {
        this(context, null);
    }

    public TintProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (attrs == null) {
            backgroundTintResId = 0;
            progressTintResId = R.color.default_color_primary;
            progressBackgroundTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintSeekbar, 0, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_seekbarBackgroundTint, 0);
        progressTintResId = array.getResourceId(R.styleable.TintSeekbar_progressTint, R.color.default_color_primary);
        progressBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_progressBackgroundTint, 0);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        if (backgroundTintResId != 0) {
            int backgroundColor = ColorStateListUtils.resolveColor(getContext(), backgroundTintResId, isInEditMode());
            if (getBackground() == null) {
                setBackgroundColor(backgroundColor);
            } else {
                setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
            }
        }
        int progressColor = ColorStateListUtils.resolveColor(getContext(), progressTintResId, isInEditMode());
        setProgressTintList(ColorStateList.valueOf(progressColor));
        setIndeterminateTintList(ColorStateList.valueOf(progressColor));
        if (progressBackgroundTintResId != 0) {
            int progressBackgroundColor = ColorStateListUtils.resolveColor(getContext(), progressBackgroundTintResId, isInEditMode());
            setProgressBackgroundTintList(ColorStateList.valueOf(progressBackgroundColor));
        }
    }

    public TintProgressBar setBackgroundTintResId(int resId) {
        backgroundTintResId = resId;
        tint();
        return this;
    }

    public TintProgressBar setProgressTintResId(int resId) {
        progressTintResId = resId;
        tint();
        return this;
    }

    public TintProgressBar setProgressBackgroundTintResId(int resId) {
        progressBackgroundTintResId = resId;
        tint();
        return this;
    }
}
