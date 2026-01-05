package com.huanchengfly.tieba.core.ui.view.tint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huanchengfly.tieba.core.ui.view.R;
import com.huanchengfly.tieba.core.ui.view.tint.Tintable;
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeColorResolver;

public class TintSwipeRefreshLayout extends SwipeRefreshLayout implements Tintable {
    public TintSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public TintSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        applyTintColor();
    }

    private void applyTintColor() {
        setColorSchemeColors(ThemeColorResolver.accentColor(getContext()));
        setProgressBackgroundColorSchemeColor(ThemeColorResolver.indicatorColor(getContext()));
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
