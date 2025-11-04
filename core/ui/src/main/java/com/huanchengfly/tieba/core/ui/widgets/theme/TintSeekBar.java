package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils;
import com.huanchengfly.tieba.core.ui.theme.Tintable;

@SuppressLint("CustomViewStyleable")
public class TintSeekBar extends AppCompatSeekBar implements Tintable {
    private int backgroundTintResId;
    private int progressTintResId;
    private int progressBackgroundTintResId;
    private int thumbColorResId;

    public TintSeekBar(@NonNull Context context) {
        this(context, null);
    }

    public TintSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            backgroundTintResId = 0;
            progressTintResId = 0;
            progressBackgroundTintResId = 0;
            thumbColorResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintSeekbar, defStyleAttr, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_seekbarBackgroundTint, 0);
        progressTintResId = array.getResourceId(R.styleable.TintSeekbar_progressTint, 0);
        progressBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_progressBackgroundTint, 0);
        thumbColorResId = array.getResourceId(R.styleable.TintSeekbar_thumbColor, 0);
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
        if (progressTintResId != 0) {
            int progressColor = ColorStateListUtils.resolveColor(getContext(), progressTintResId, isInEditMode());
            setProgressTintList(ColorStateList.valueOf(progressColor));
        }
        if (progressBackgroundTintResId != 0) {
            int progressBackgroundColor = ColorStateListUtils.resolveColor(getContext(), progressBackgroundTintResId, isInEditMode());
            setProgressBackgroundTintList(ColorStateList.valueOf(progressBackgroundColor));
        }
        if (thumbColorResId != 0) {
            setThumbTintList(ColorStateListUtils.getColorStateList(getContext(), thumbColorResId, isInEditMode()));
        }
    }
}
