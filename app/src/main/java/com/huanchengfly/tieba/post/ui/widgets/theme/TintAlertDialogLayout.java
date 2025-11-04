package com.huanchengfly.tieba.post.ui.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AlertDialogLayout;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.core.ui.theme.Tintable;
import com.huanchengfly.tieba.post.ui.common.theme.ThemeColorResolver;
import com.huanchengfly.tieba.post.ui.common.theme.ThemeDrawableUtils;

@SuppressLint("RestrictedApi")
public class TintAlertDialogLayout extends AlertDialogLayout implements Tintable {
    private int mBackgroundTintResId;

    public TintAlertDialogLayout(@Nullable Context context) {
        this(context, null);
    }

    public TintAlertDialogLayout(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = R.color.default_color_background;
            applyTintColor();
            return;
        }
        @SuppressLint("CustomViewStyleable") TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, 0, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.default_color_background);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        if (getBackground() == null) {
            setBackgroundColor(ThemeColorResolver.colorById(getContext(), mBackgroundTintResId));
        } else {
            setBackground(ThemeDrawableUtils.tint(getBackground(), ThemeColorResolver.colorById(getContext(), mBackgroundTintResId)));
        }
        if (ThemeColorResolver.state(getContext()).isTranslucent()) {
            setBackgroundTintList(null);
            setBackgroundColor(ThemeColorResolver.colorById(getContext(), R.color.theme_color_card_grey_dark));
        }
    }
}
