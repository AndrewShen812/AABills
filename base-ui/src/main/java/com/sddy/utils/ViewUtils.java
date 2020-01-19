package com.sddy.utils;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.design.widget.TabLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sddy.baseui.BaseApplication;

import java.lang.reflect.Field;

public class ViewUtils {

    public static void setTabLayoutIndicator(TabLayout tabs, int leftDip, int rightDip) {
        try {
            Class<?> tabLayout = tabs.getClass();
            Field tabStrip = null;
            try {
                tabStrip = tabLayout.getDeclaredField("mTabStrip");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            tabStrip.setAccessible(true);
            LinearLayout llTab = null;
            try {
                llTab = (LinearLayout) tabStrip.get(tabs);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftDip, Resources.getSystem().getDisplayMetrics());
            int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightDip, Resources.getSystem().getDisplayMetrics());

            for (int i = 0; i < llTab.getChildCount(); i++) {
                View child = llTab.getChildAt(i);
                child.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.leftMargin = left;
                params.rightMargin = right;
                child.setLayoutParams(params);
                child.invalidate();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static SpannableString getColoredText(String orgText, int start, @ColorRes int colorRes) {
        Resources res = BaseApplication.getInstance().getResources();
        if (TextUtils.isEmpty(orgText) || start >= orgText.length()) {
            return new SpannableString("");
        }
        SpannableString string = new SpannableString(orgText);
        string.setSpan(new ForegroundColorSpan(res.getColor(colorRes)), start, orgText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return string;
    }

    public static void setClickableText(TextView textView, String orgText, int start, @ColorRes final int colorRes,
                                        final boolean showUnderLine, final View.OnClickListener clickListener) {
        final Resources res = BaseApplication.getInstance().getResources();
        if (textView == null || TextUtils.isEmpty(orgText) || start >= orgText.length()) {
            return;
        }
        SpannableString string = getColoredText(orgText, start, colorRes);
        string.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (clickListener != null) {
                    clickListener.onClick(widget);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(res.getColor(colorRes));
                ds.setUnderlineText(showUnderLine);
            }
        }, start, orgText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(string);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static GradientDrawable getDrawableBg(@ColorRes int colorRes) {
        int color = BaseApplication.getInstance().getResources().getColor(colorRes);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        return drawable;
    }

    public static GradientDrawable getMultiStateBg(@ColorRes int normalColor,
                                                   @ColorRes int pressColor) {
        Resources res = BaseApplication.getInstance().getResources();
        GradientDrawable drawable = new GradientDrawable();
        // state前面加-表示false状态
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {}
        };
        // 背景颜色
        int[] colors = new int[] {res.getColor(pressColor),
                res.getColor(normalColor)};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        drawable.setColor(colorStateList);
        return drawable;
    }

    public static GradientDrawable getMultiStateBg(@ColorRes int normalColor,
                                                   @ColorRes int pressColor,
                                                   @ColorRes int disableColor,
                                                   @DimenRes int cornerR) {
        Resources res = BaseApplication.getInstance().getResources();
        GradientDrawable drawable = new GradientDrawable();
        // state前面加-表示false状态
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {-android.R.attr.state_enabled},
                new int[] {}
        };
        // 背景颜色
        int[] colors = new int[] {res.getColor(pressColor),
                res.getColor(disableColor),
                res.getColor(normalColor)};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        drawable.setColor(colorStateList);
        drawable.setCornerRadius(res.getDimensionPixelSize(cornerR));
        return drawable;
    }

    public static GradientDrawable getMultiStateBg(@ColorRes int normalColor,
                                                   @ColorRes int pressColor,
                                                   @ColorRes int disableColor,
                                                   @DimenRes int cornerR,
                                                   @DimenRes int strokeW,
                                                   @ColorRes int strokeColor,
                                                   @ColorRes int disableStrokeColor) {
        Resources res = BaseApplication.getInstance().getResources();
        GradientDrawable drawable = new GradientDrawable();
        // state前面加-表示false状态
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {-android.R.attr.state_enabled},
                new int[] {}
        };
        // 背景颜色
        int[] colors = new int[] {res.getColor(pressColor),
                res.getColor(disableColor),
                res.getColor(normalColor)};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        drawable.setColor(colorStateList);
        // 边框颜色
        colors = new int[] {res.getColor(strokeColor),
                res.getColor(disableStrokeColor),
                res.getColor(strokeColor)};
        ColorStateList strokeColors = new ColorStateList(states, colors);
        drawable.setStroke(res.getDimensionPixelSize(strokeW), strokeColors);
        drawable.setCornerRadius(res.getDimensionPixelSize(cornerR));
        return drawable;
    }

    public static GradientDrawable getCheckableBg(@ColorRes int normalColor,
                                                   @ColorRes int checkedColor,
                                                   @DimenRes int cornerR,
                                                   @DimenRes int strokeW,
                                                   @ColorRes int normalStrokeColor,
                                                   @ColorRes int checkedStrokeColor) {
        Resources res = BaseApplication.getInstance().getResources();
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(getCheckableColors(normalColor, checkedColor));
        drawable.setStroke(res.getDimensionPixelSize(strokeW), getCheckableColors(normalStrokeColor, checkedStrokeColor));
        drawable.setCornerRadius(res.getDimensionPixelSize(cornerR));
        return drawable;
    }

    public static ColorStateList getCheckableColors(@ColorRes int normalColor, @ColorRes int checkedColor) {
        Resources res = BaseApplication.getInstance().getResources();
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_checked},
                new int[] {android.R.attr.state_pressed},
                new int[] {}
        };
        int[] colors = new int[] {res.getColor(checkedColor),
                res.getColor(checkedColor),
                res.getColor(normalColor)};
        return new ColorStateList(states, colors);
    }

    public static StateListDrawable getCheckableDrawable(@DrawableRes int normalRes, @DrawableRes int checkedRes) {
        StateListDrawable drawable = new StateListDrawable();
        Resources res = BaseApplication.getInstance().getResources();
        drawable.addState(new int[] {android.R.attr.state_checked},
                res.getDrawable(checkedRes));
        drawable.addState(new int[] {}, res.getDrawable(normalRes));
        return drawable;
    }

    public static GradientDrawable getDrawableBg(@ColorRes int colorRes, @DimenRes int cornerRes) {
        GradientDrawable drawable = getDrawableBg(colorRes);
        Resources res = BaseApplication.getInstance().getResources();
        drawable.setCornerRadius(res.getDimensionPixelSize(cornerRes));
        return drawable;
    }
    public static GradientDrawable getDrawableBg(@ColorRes int normalColorRes, @ColorRes int pressedColorRes, @DimenRes int cornerRes) {
        GradientDrawable drawable = new GradientDrawable();
        Resources res = BaseApplication.getInstance().getResources();
        drawable.setColor(getCheckableColors(normalColorRes, pressedColorRes));
        drawable.setCornerRadius(res.getDimensionPixelSize(cornerRes));
        return drawable;
    }

    public static GradientDrawable getDrawableBg(@ColorRes int color, @DimenRes int cornerR,
                                                 @ColorRes int strokeColor, @DimenRes int strokeW) {
        Resources res = BaseApplication.getInstance().getResources();
        GradientDrawable drawable = getDrawableBg(color, cornerR);
        drawable.setStroke(res.getDimensionPixelSize(strokeW), res.getColor(strokeColor));
        return drawable;
    }
}
