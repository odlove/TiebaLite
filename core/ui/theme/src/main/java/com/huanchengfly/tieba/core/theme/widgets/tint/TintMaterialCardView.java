package com.huanchengfly.tieba.core.theme.widgets.tint
;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.huanchengfly.tieba.core.theme.R;
import com.huanchengfly.tieba.core.theme.widgets.tint.Tintable;
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeColorResolver;

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
        int background = ThemeColorResolver.colorById(getContext(), R.color.sem_surface_card);
        setCardBackgroundColor(background);
        setStrokeColor(ThemeColorResolver.colorById(getContext(), R.color.sem_outline_low));
    }
}
