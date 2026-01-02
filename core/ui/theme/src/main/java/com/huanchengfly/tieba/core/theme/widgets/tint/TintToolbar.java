package com.huanchengfly.tieba.core.theme.widgets.tint
;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.huanchengfly.tieba.core.theme.R;
import com.huanchengfly.tieba.core.theme.widgets.tint.Tintable;
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeColorResolver;
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeDrawableUtils;

public class TintToolbar extends Toolbar implements Tintable {
    public static final String TAG = "TintToolbar";

    private int mBackgroundTintResId;
    private int mItemTintResId;
    private int mSecondaryItemTintResId;
    private int mActiveItemTintResId;

    public TintToolbar(Context context) {
        this(context, null);
    }

    public TintToolbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.toolbarStyle);
    }

    @SuppressLint("CustomViewStyleable")
    public TintToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mItemTintResId = R.color.sem_content_primary;
            mSecondaryItemTintResId = R.color.sem_content_secondary;
            mActiveItemTintResId = R.color.sem_state_active;
            applyTintColor();
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TintToolbar, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintToolbar_toolbarBackgroundTint, 0);
        mItemTintResId = array.getResourceId(R.styleable.TintToolbar_itemTint, R.color.sem_content_primary);
        mSecondaryItemTintResId = array.getResourceId(R.styleable.TintToolbar_secondaryItemTint, R.color.sem_content_secondary);
        mActiveItemTintResId = array.getResourceId(R.styleable.TintToolbar_activeItemTint, R.color.sem_state_active);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        setTitleTextAppearance(getContext(), R.style.TextAppearance_Title);
        setSubtitleTextAppearance(getContext(), R.style.TextAppearance_Subtitle);
        fixColor();
        tintBackground();
        tintNavigationIcon();
        tintOverflowIcon();
        tintMenuIcon();
        setTitleTextColor(ThemeColorResolver.colorById(getContext(), mItemTintResId));
        setSubtitleTextColor(ThemeColorResolver.colorById(getContext(), mSecondaryItemTintResId));
    }

    private void fixColor() {
        if (mItemTintResId == 0) {
            mItemTintResId = R.color.sem_content_primary;
        }
        if (mSecondaryItemTintResId == 0) {
            mSecondaryItemTintResId = R.color.sem_content_secondary;
        }
        if (mActiveItemTintResId == 0) {
            mActiveItemTintResId = R.color.sem_state_active;
        }
    }

    private void tintBackground() {
        if (mBackgroundTintResId != 0) {
            if (getBackground() == null) {
                setBackgroundColor(ThemeColorResolver.colorById(getContext(), mBackgroundTintResId));
            } else {
                setBackgroundTintList(ColorStateList.valueOf(ThemeColorResolver.colorById(getContext(), mBackgroundTintResId)));
            }
        }
    }

    @Override
    public void setNavigationIcon(int resId) {
        super.setNavigationIcon(resId);
        applyTintColor();
    }

    private void tintMenuIcon() {
        for (int i = 0; i < getMenu().size(); i++) {
            MenuItem menuItem = getMenu().getItem(i);
            Drawable drawable = menuItem.getIcon();
            if (drawable == null) {
                continue;
            }
            int[][] states = new int[3][];
            states[0] = new int[]{android.R.attr.state_checked};
            states[1] = new int[]{android.R.attr.state_enabled};
            states[2] = new int[]{};
            ColorStateList colorStateList = new ColorStateList(states, new int[]{
                ThemeColorResolver.colorById(getContext(), mActiveItemTintResId),
                ThemeColorResolver.colorById(getContext(), mItemTintResId),
                ThemeColorResolver.colorById(getContext(), mSecondaryItemTintResId),
            });
            drawable.setTintList(colorStateList);
            drawable.invalidateSelf();
            menuItem.setIcon(drawable);
            Log.i(TAG, "tintMenuIcon: " + i + "finish");
        }
    }

    private void tintNavigationIcon() {
        Drawable drawable = getNavigationIcon();
        if (drawable == null) {
            return;
        }
        setNavigationIcon(ThemeDrawableUtils.tint(drawable, ThemeColorResolver.colorById(getContext(), mItemTintResId)));
    }

    private void tintOverflowIcon() {
        Drawable drawable = getOverflowIcon();
        if (drawable == null) {
            return;
        }
        setOverflowIcon(ThemeDrawableUtils.tint(drawable, ThemeColorResolver.colorById(getContext(), mItemTintResId)));
    }

    @Override
    public void inflateMenu(int resId) {
        super.inflateMenu(resId);
        applyTintColor();
    }

    public void setBackgroundTintResId(int backgroundTintResId) {
        this.mBackgroundTintResId = backgroundTintResId;
        tint();
    }

    public void setItemTintResId(int itemTintResId) {
        this.mItemTintResId = itemTintResId;
        tint();
    }

    public void setSecondaryItemTintResId(int secondaryItemTintResId) {
        this.mSecondaryItemTintResId = secondaryItemTintResId;
        tint();
    }

    public void setActiveItemTintResId(int activeItemTintResId) {
        this.mActiveItemTintResId = activeItemTintResId;
        tint();
    }
}
