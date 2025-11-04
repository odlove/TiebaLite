package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils;
import com.huanchengfly.tieba.core.ui.theme.Tintable;

@SuppressLint("CustomViewStyleable")
public class TintImageView extends AppCompatImageView implements Tintable, BackgroundTintable {
    private int tintListResId;
    private int backgroundTintResId;

    public TintImageView(Context context) {
        this(context, null);
    }

    public TintImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs == null) {
            backgroundTintResId = 0;
            tintListResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintImageView, defStyleAttr, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintImageView_backgroundTint, 0);
        tintListResId = array.getResourceId(R.styleable.TintImageView_tint, 0);
        array.recycle();
        applyTintColor();
    }

    public void setTintListResId(int tintListResId) {
        this.tintListResId = tintListResId;
        applyTintColor();
    }

    private void applyTintColor() {
        if (backgroundTintResId != 0) {
            if (getBackground() == null) {
                setBackground(new ColorDrawable(Color.BLACK));
            }
            setBackgroundTintList(ColorStateListUtils.getColorStateList(getContext(), backgroundTintResId, isInEditMode()));
        }
        if (tintListResId != 0) {
            setImageTintList(ColorStateListUtils.getColorStateList(getContext(), tintListResId, isInEditMode()));
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    @Override
    public int getBackgroundTintResId() {
        return backgroundTintResId;
    }

    @Override
    public void setBackgroundTintResId(int resId) {
        backgroundTintResId = resId;
        applyTintColor();
    }
}
