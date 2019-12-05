package com.zhanyage.htmlparselib.api;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Html 中关于 img 、video 标签加载的接口
 */
public interface HtmlImageLoader {

    /**
     * 外部图片加载完成的回调，外部可通过该 callback 把加载完成的 bitmap 回调给
     * htmlparse 内部
     */
    interface Callback {
        /**
         * 加载成功的回调
         * @param bitmap 加载成功的 bitmap
         */
        void onLoadComplete(Bitmap bitmap);

        /**
         * 加载失败的回调
         */
        void onLoadFailed();
    }

    /**
     * 加载 html 中 img 标签中图片回调的方法
     * @param url 图片的 url
     * @param callback 加载完成的 callback
     */
    void loadImage(String url, Callback callback);

    /**
     * 加载 html 中的 video 标签中图片的回调方法
     * @param url video 标签首帧图片的 url
     * @param callback 加载完成的 callback
     */
    void loadVideoImage(String url, Callback callback);

    /**
     * video、img 标签默认展示图片的回调
     * @return 默认展示图片的 drawable
     */
    Drawable getDefaultDrawable();

    /**
     * video、img 失败展示的图片的回调
     * @return
     */
    Drawable getErrorDrawable();

    /**
     * 获取图片展示的最大宽度
     * @return 最大宽度的 px
     */
    int getMaxWidth();

    /**
     * 是否适配宽度，如果宽度超过最大宽度，是否等比例缩放图片
     * @return true or false
     */
    boolean fitWidth();

}
