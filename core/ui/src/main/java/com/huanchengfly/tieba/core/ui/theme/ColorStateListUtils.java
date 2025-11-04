package com.huanchengfly.tieba.core.ui.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.TypedValue;
import android.util.Xml;

import androidx.core.graphics.ColorUtils;
import androidx.core.content.ContextCompat;

import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedList;

public final class ColorStateListUtils {
    private ColorStateListUtils() {}

    public static ColorStateList createColorStateList(Context context, int resId) {
        return createColorStateList(context, resId, false);
    }

    public static ColorStateList createColorStateList(Context context, int resId, boolean isInEditMode) {
        if (resId <= 0) return null;

        if (isInEditMode) {
            ColorStateList previewList = ContextCompat.getColorStateList(context, resId);
            if (previewList != null) {
                return previewList;
            }
        }

        if (resId <= 0) return null;

        TypedValue value = new TypedValue();
        context.getResources().getValue(resId, value, true);
        ColorStateList cl = null;
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            cl = ColorStateList.valueOf(resolveColorInternal(context, value.resourceId));
        } else {
            CharSequence fileName = value.string;
            if (fileName == null) {
                return null;
            }
            String file = fileName.toString();
            if (file.endsWith("xml")) {
                try (XmlResourceParser parser = context.getResources().getAssets()
                    .openXmlResourceParser(value.assetCookie, file)) {
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    int type;
                    while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                        // Skip until start tag.
                    }
                    if (type != XmlPullParser.START_TAG) {
                        throw new XmlPullParserException("No start tag found");
                    }
                    cl = createFromXmlInner(context, parser, attrs);
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }
        if (cl == null) {
            cl = ContextCompat.getColorStateList(context, resId);
        }
        return cl;
    }

    public static ColorStateList getColorStateList(Context context, int resId, boolean isInEditMode) {
        return createColorStateList(context, resId, isInEditMode);
    }

    private static ColorStateList createFromXmlInner(Context context, XmlPullParser parser, AttributeSet attrs)
        throws IOException, XmlPullParserException {
        if (!"selector".equals(parser.getName())) {
            throw new XmlPullParserException(
                parser.getPositionDescription() + ": invalid color state list tag " + parser.getName());
        }
        return inflateColorStateList(context, parser, attrs);
    }

    private static ColorStateList inflateColorStateList(Context context, XmlPullParser parser, AttributeSet attrs)
        throws IOException, XmlPullParserException {
        final int innerDepth = parser.getDepth() + 1;
        LinkedList<int[]> stateList = new LinkedList<>();
        LinkedList<Integer> colorList = new LinkedList<>();

        int type;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT &&
            ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG || depth > innerDepth || !"item".equals(parser.getName())) {
                continue;
            }

            TypedArray a1 = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.color});
            final int value = a1.getResourceId(0, android.graphics.Color.MAGENTA);
            final int baseColor = value == android.graphics.Color.MAGENTA
                ? android.graphics.Color.MAGENTA
                : resolveColorInternal(context, value);
            a1.recycle();

            TypedArray a2 = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.alpha});
            final float alphaMod = a2.getFloat(0, 1.0f);
            a2.recycle();

            int finalColor = alphaMod != 1.0f
                ? ColorUtils.setAlphaComponent(baseColor,
                    Math.round(android.graphics.Color.alpha(baseColor) * alphaMod))
                : baseColor;
            colorList.add(finalColor);
            stateList.add(extractStateSet(attrs));
        }

        if (!stateList.isEmpty() && stateList.size() == colorList.size()) {
            int[] colors = new int[colorList.size()];
            for (int i = 0; i < colorList.size(); i++) {
                colors[i] = colorList.get(i);
            }
            return new ColorStateList(stateList.toArray(new int[stateList.size()][]), colors);
        }
        return null;
    }

    private static int[] extractStateSet(AttributeSet attrs) {
        int j = 0;
        final int numAttrs = attrs.getAttributeCount();
        int[] states = new int[numAttrs];
        for (int i = 0; i < numAttrs; i++) {
            final int stateResId = attrs.getAttributeNameResource(i);
            switch (stateResId) {
                case 0:
                    break;
                case android.R.attr.color:
                case android.R.attr.alpha:
                    continue;
                default:
                    states[j++] = attrs.getAttributeBooleanValue(i, false)
                        ? stateResId : -stateResId;
            }
        }
        return StateSet.trimStateSet(states, j);
    }

    public static int resolveColor(Context context, int colorResId, boolean isInEditMode) {
        if (colorResId <= 0) {
            return android.graphics.Color.TRANSPARENT;
        }
        if (isInEditMode) {
            return ContextCompat.getColor(context, colorResId);
        }
        return resolveColorInternal(context, colorResId);
    }

    private static int resolveColorInternal(Context context, int colorResId) {
        try {
            return ThemeColorResolver.colorById(context, colorResId);
        } catch (IllegalStateException | NullPointerException ignored) {
            return ContextCompat.getColor(context, colorResId);
        }
    }
}
