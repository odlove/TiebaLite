package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.widget.CompoundButtonCompat;

import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils;

public class TintCheckBox extends AppCompatCheckBox {
    private int backgroundTintResId;
    private int textColorResId;
    private int buttonTintResId;

    public TintCheckBox(Context context) {
        this(context, null);
    }

    public TintCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkboxStyle);
    }

    public TintCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs == null) {
            backgroundTintResId = 0;
            buttonTintResId = 0;
            textColorResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintCheckBox, defStyleAttr, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintCheckBox_checkboxBackgroundTint, 0);
        buttonTintResId = array.getResourceId(R.styleable.TintCheckBox_buttonTint, 0);
        textColorResId = array.getResourceId(R.styleable.TintCheckBox_textColor, 0);
        array.recycle();
        applyTintColor();
    }

    private void applyTintColor() {
        if (backgroundTintResId != 0) {
            setBackgroundTintList(ColorStateListUtils.getColorStateList(getContext(), backgroundTintResId, isInEditMode()));
        }
        if (buttonTintResId != 0) {
            CompoundButtonCompat.setButtonTintList(this, ColorStateListUtils.getColorStateList(getContext(), buttonTintResId, isInEditMode()));
        }
        if (textColorResId != 0) {
            setTextColor(ColorStateListUtils.getColorStateList(getContext(), textColorResId, isInEditMode()));
        }
    }
}
