package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.navigation.NavigationView;
import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.Tintable;
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver;

public class TintNavigationView extends NavigationView implements Tintable {
    private int backgroundTintResId;
    private int itemIconTintResId;
    private int itemTextTintResId;

    public TintNavigationView(Context context) {
        this(context, null);
    }

    public TintNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            backgroundTintResId = R.color.transparent;
            itemIconTintResId = 0;
            itemTextTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintNavigationView, defStyleAttr, 0);
        backgroundTintResId = array.getResourceId(R.styleable.TintNavigationView_navigationBackgroundTint, R.color.transparent);
        itemIconTintResId = array.getResourceId(R.styleable.TintNavigationView_itemIconTint, 0);
        itemTextTintResId = array.getResourceId(R.styleable.TintNavigationView_itemTextTint, 0);
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
        if (itemIconTintResId != 0) {
            setItemIconTintList(ColorStateList.valueOf(ThemeColorResolver.colorById(getContext(), itemIconTintResId)));
        } else {
            setItemIconTintList(null);
        }
        if (itemTextTintResId != 0) {
            setItemTextColor(ColorStateList.valueOf(ThemeColorResolver.colorById(getContext(), itemTextTintResId)));
        } else {
            setItemTextColor(null);
        }
    }
}
