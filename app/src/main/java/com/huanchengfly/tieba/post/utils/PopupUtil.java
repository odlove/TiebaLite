package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.MenuPopupWindow;
import androidx.appcompat.widget.PopupMenu;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.di.entrypoints.ThemeControllerEntryPoint;
import com.huanchengfly.tieba.post.ui.common.theme.ThemeBridge;
import com.huanchengfly.tieba.post.ui.common.theme.ThemeDrawableUtils;

import dagger.hilt.android.EntryPointAccessors;

import java.lang.reflect.Field;

@SuppressLint("RestrictedApi")
public class PopupUtil {
    private PopupUtil() {
    }

    public static void replaceBackground(ListPopupWindow listPopupWindow) {
        try {
            Field contextField = ListPopupWindow.class.getDeclaredField("mContext");
            contextField.setAccessible(true);
            Context context = (Context) contextField.get(listPopupWindow);
            ThemeBridge bridge = EntryPointAccessors.fromApplication(context.getApplicationContext(), ThemeControllerEntryPoint.class).themeBridge();
            int backgroundColor = bridge.colorByAttr(context, R.attr.colorCard);
            listPopupWindow.setBackgroundDrawable(
                    ThemeDrawableUtils.tint(AppCompatResources.getDrawable(context, R.drawable.bg_popup), backgroundColor)
            );
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void replaceBackground(PopupMenu popupMenu) {
        try {
            Field contextField = PopupMenu.class.getDeclaredField("mContext");
            contextField.setAccessible(true);
            Context context = (Context) contextField.get(popupMenu);
            Field field = PopupMenu.class.getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper menuPopupHelper = (MenuPopupHelper) field.get(popupMenu);
            Object obj = menuPopupHelper.getPopup();
            Field popupField = obj.getClass().getDeclaredField("mPopup");
            popupField.setAccessible(true);
            MenuPopupWindow menuPopupWindow = (MenuPopupWindow) popupField.get(obj);
            ThemeBridge bridge = EntryPointAccessors.fromApplication(context.getApplicationContext(), ThemeControllerEntryPoint.class).themeBridge();
            int backgroundColor = bridge.colorByAttr(context, R.attr.colorCard);
            menuPopupWindow.setBackgroundDrawable(
                    ThemeDrawableUtils.tint(context.getDrawable(R.drawable.bg_popup), backgroundColor)
            );
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static PopupMenu create(View anchor) {
        PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
        replaceBackground(popupMenu);
        return popupMenu;
    }
}
