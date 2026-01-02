package com.huanchengfly.tieba.post.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.huanchengfly.tieba.core.common.utils.DateTimeUtils;
import com.huanchengfly.tieba.core.theme.runtime.controller.ThemeController;
import com.huanchengfly.tieba.core.theme.model.ThemeTokens;
import com.huanchengfly.tieba.core.theme.runtime.entrypoints.ThemeRuntimeEntryPoint;

import java.util.Locale;

import dagger.hilt.android.EntryPointAccessors;

public class TiebaLiteJavaScript {
    public static final String TAG = "JsBridge";
    private static final String SP_WEBVIEW_INFO = "webview_info";

    private static final Handler handler = new Handler();
    public Context context;
    public WebView webView;

    public TiebaLiteJavaScript(WebView webView) {
        this.context = webView.getContext();
        this.webView = webView;
    }

    @JavascriptInterface
    public void toast(final String text) {
        handler.post(() -> {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        });
    }

    @JavascriptInterface
    public String getTimeFromNow(String time) {
        return DateTimeUtils.getRelativeTimeString(context, time);
    }

    @JavascriptInterface
    public String getTheme() {
        ThemeController controller = EntryPointAccessors.fromApplication(
                context.getApplicationContext(),
                ThemeRuntimeEntryPoint.class
        ).themeController();
        String rawTheme = controller.getThemeState().getValue().getRawTheme();
        if (rawTheme == null || rawTheme.isEmpty()) {
            return ThemeTokens.THEME_DEFAULT;
        }
        return rawTheme.toLowerCase(Locale.getDefault());
    }

    @JavascriptInterface
    public void copyText(String content) {
        copyTextInternal(content);
    }

    @JavascriptInterface
    public void putString(String key, String value) {
        getPrefs()
                .edit()
                .putString(key, value)
                .apply();
        Log.i(TAG, "putString: " + key + ": " + value);
    }

    @JavascriptInterface
    public String getString(String key) {
        return getPrefs().getString(key, "");
    }

    @JavascriptInterface
    public int getInt(String key, int defValue) {
        return getPrefs().getInt(key, defValue);
    }

    @JavascriptInterface
    public void putInt(String key, int value) {
        getPrefs()
                .edit()
                .putInt(key, value)
                .apply();
        Log.i(TAG, "putInt: " + key + ": " + value);
    }

    private SharedPreferences getPrefs() {
        return context.getSharedPreferences(SP_WEBVIEW_INFO, Context.MODE_PRIVATE);
    }

    private void copyTextInternal(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        ClipData clipData = ClipData.newPlainText("Tieba Lite", text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PersistableBundle extras = new PersistableBundle();
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false);
            clipData.getDescription().setExtras(extras);
        }
        clipboard.setPrimaryClip(clipData);
    }
}
