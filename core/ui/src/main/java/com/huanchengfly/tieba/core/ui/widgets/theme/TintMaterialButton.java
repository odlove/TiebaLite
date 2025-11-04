package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils;
import com.huanchengfly.tieba.core.ui.theme.Tintable;

public class TintMaterialButton extends MaterialButton implements Tintable {
    private int backgroundTintResId;
    private int textColorResId;
    private int strokeColorResId;

    public TintMaterialButton(@NonNull Context context) {
        this(context, null);
    }

    public TintMaterialButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintMaterialButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            backgroundTintResId = 0;
            textColorResId = 0;
            strokeColorResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintMaterialButton, defStyleAttr, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintMaterialButton_buttonBackgroundTint, 0);
        textColorResId = array.getResourceId(R.styleable.TintMaterialButton_buttonTextColor, 0);
        strokeColorResId = array.getResourceId(R.styleable.TintMaterialButton_buttonStrokeColor, 0);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        if (textColorResId != 0) {
            setTextColor(ColorStateListUtils.getColorStateList(getContext(), textColorResId, isInEditMode()));
        }
        if (backgroundTintResId != 0) {
            setBackgroundTintList(ColorStateListUtils.getColorStateList(getContext(), backgroundTintResId, isInEditMode()));
        }
        if (strokeColorResId != 0) {
            setStrokeColor(ColorStateListUtils.getColorStateList(getContext(), strokeColorResId, isInEditMode()));
        }
    }

    public void setTextColorResId(int textColorResId) {
        this.textColorResId = textColorResId;
        applyTintColor();
    }

    public void setBackgroundTintResId(int backgroundTintResId) {
        this.backgroundTintResId = backgroundTintResId;
        applyTintColor();
    }
}
