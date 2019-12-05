package com.zhanyage.htmlparse.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtils {
    private static Context context;


    public static void init(Context context) {
        ScreenUtils.context = context.getApplicationContext();
    }

    public static int dp2px(float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public static int px2dp(float pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    public static int px2sp(float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int getScreenWidth() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getScreenMin() {
        return Math.min(getScreenWidth(), getScreenHeight());
    }

}
