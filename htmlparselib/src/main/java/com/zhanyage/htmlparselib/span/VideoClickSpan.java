package com.zhanyage.htmlparselib.span;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.zhanyage.htmlparselib.api.OnTagClickListener;

/**
 * Created by zhanyage on 2019-12-02
 * Describe:
 */
public class VideoClickSpan extends ClickableSpan {

    private OnTagClickListener listener;
    private Context context;
    private String videoUrl;

    public VideoClickSpan(Context context, String videoUrl) {
        this.context = context;
        this.videoUrl = videoUrl;
    }

    public void setListener(OnTagClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View widget) {
        if (listener != null) {
            listener.onVideoClick(context, videoUrl);
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(false);
    }

}
