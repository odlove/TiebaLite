package com.huanchengfly.tieba.core.ui.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.graphics.drawable.Drawable;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.widget.TextViewCompat;

import com.huanchengfly.tieba.core.ui.R;
import com.huanchengfly.tieba.core.ui.theme.ColorStateListUtils;
import com.huanchengfly.tieba.core.ui.theme.Tintable;
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeDrawableUtils;
import com.huanchengfly.tieba.core.ui.widgets.edittext.OperationManager;

public class TintUndoableEditText extends AppCompatEditText implements Tintable {
    private static final String KEY_SUPER = "KEY_SUPER";
    private static final String KEY_OPT = OperationManager.class.getCanonicalName();
    private final OperationManager mgr = new OperationManager(this);
    private int textColorResId;
    private int hintTextColorResId;
    private int cursorColorResId;

    public TintUndoableEditText(Context context) {
        super(context);
        init(null, 0);
    }

    public TintUndoableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TintUndoableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public OperationManager getMgr() {
        return mgr;
    }

    public boolean canUndo() {
        return mgr.canUndo();
    }

    public boolean canRedo() {
        return mgr.canRedo();
    }

    public boolean undo() {
        return mgr.undo();
    }

    public boolean redo() {
        return mgr.redo();
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        if (!isInEditMode()) {
            TextViewCompat.setCustomSelectionActionModeCallback(this, new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater menuInflater = mode.getMenuInflater();
                    menuInflater.inflate(R.menu.menu_undoable_edit_text, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }
            });
            addTextChangedListener(mgr);
        }

        if (attrs == null) {
            textColorResId = 0;
            hintTextColorResId = 0;
            cursorColorResId = 0;
            tint();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintUndoableEditText, defStyleAttr, 0);
        textColorResId = array.getResourceId(R.styleable.TintUndoableEditText_textColor, 0);
        hintTextColorResId = array.getResourceId(R.styleable.TintUndoableEditText_hintTextColor, 0);
        cursorColorResId = array.getResourceId(R.styleable.TintUndoableEditText_cursorColor, 0);
        array.recycle();
        tint();
    }

    @Override
    public void tint() {
        if (textColorResId != 0) {
            setTextColor(ColorStateListUtils.getColorStateList(getContext(), textColorResId, isInEditMode()));
        }
        if (hintTextColorResId != 0) {
            setHintTextColor(ColorStateListUtils.getColorStateList(getContext(), hintTextColorResId, isInEditMode()));
        }
        if (cursorColorResId != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Drawable drawable = getTextCursorDrawable();
            if (drawable != null) {
                setTextCursorDrawable(ThemeDrawableUtils.tint(
                    drawable,
                    ColorStateListUtils.getColorStateList(getContext(), cursorColorResId, isInEditMode())
                ));
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER, super.onSaveInstanceState());
        bundle.putBundle(KEY_OPT, mgr.exportState());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superState = bundle.getParcelable(KEY_SUPER);

        mgr.disable();
        super.onRestoreInstanceState(superState);
        mgr.enable();

        mgr.importState(bundle.getBundle(KEY_OPT));
    }

    public void setTextColorResId(int textColorResId) {
        this.textColorResId = textColorResId;
        tint();
    }

    public void setHintTextColorResId(int hintTextColorResId) {
        this.hintTextColorResId = hintTextColorResId;
        tint();
    }
}
