package com.huanchengfly.tieba.core.ui.view.tint;
import android.app.Activity;
import android.view.View;

public interface ExtraRefreshable {
    void refreshGlobal(Activity activity);

    void refreshSpecificView(View view);
}
