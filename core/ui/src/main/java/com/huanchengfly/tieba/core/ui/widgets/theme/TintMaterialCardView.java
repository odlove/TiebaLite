package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.Tintable;
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver;

@SuppressLint("CustomViewStyleable")
public class TintMaterialCardView extends MaterialCardView implements Tintable {

    public TintMaterialCardView(@NonNull Context context) {
        this(context, null);
    }

    public TintMaterialCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintMaterialCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        int background = ThemeColorResolver.colorById(getContext(), R.color.default_color_card);
        setCardBackgroundColor(background);
        setStrokeColor(ThemeColorResolver.colorById(getContext(), R.color.default_color_divider));
    }
}
