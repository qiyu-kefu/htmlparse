package com.zhanyage.htmlparselib.api;

import android.content.Context;

import java.util.List;

/**
 * html 中点击事件的接口
 */
public interface OnTagClickListener {
    /**
     * 图片点击事件的回调
     * @param context context
     * @param imageUrlList 本段 html 中图片的 url list
     * @param position 点击第几张图片
     */
    void onImageClick(Context context, List<String> imageUrlList, int position);

    /**
     * 超链接点击事件的回调
     * @param context context
     * @param url href 中的 url
     */
    void onLinkClick(Context context, String url);

    /**
     * 视频点击事件的回调
     * @param context context
     * @param videoUrl 视频的 url
     */
    void onVideoClick(Context context, String videoUrl);
}