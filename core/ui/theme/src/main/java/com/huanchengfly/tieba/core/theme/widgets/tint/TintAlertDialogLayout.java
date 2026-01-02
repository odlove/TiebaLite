package com.huanchengfly.tieba.core.theme.widgets.tint
;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AlertDialogLayout;

import com.huanchengfly.tieba.core.theme.R;
import com.huanchengfly.tieba.core.theme.widgets.tint.Tintable;
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeColorResolver;
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeDrawableUtils;

@SuppressLint("RestrictedApi")
public class TintAlertDialogLayout extends AlertDialogLayout implements Tintable {
    private int backgroundTintResId;

    public TintAlertDialogLayout(@Nullable Context context) {
        this(context, null);
    }

    public TintAlertDialogLayout(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            backgroundTintResId = R.color.sem_surface_card;
            applyTintColor();
            return;
        }
        @SuppressLint("CustomViewStyleable") TypedArray array =
            context.obtainStyledAttributes(attrs, R.styleable.TintView, 0, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.sem_surface_card);
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
            setBackground(ThemeDrawableUtils.tint(
                getBackground(),
                ThemeColorResolver.colorById(getContext(), backgroundTintResId)
            ));
        }
        if (ThemeColorResolver.state(getContext()).isTranslucent()) {
            setBackgroundTintList(null);
            setBackgroundColor(ThemeColorResolver.colorById(getContext(), R.color.theme_color_card_grey_dark));
        }
    }
}
