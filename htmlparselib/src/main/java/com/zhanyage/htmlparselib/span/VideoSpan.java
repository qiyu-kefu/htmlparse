package com.zhanyage.htmlparselib.span;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/**
 * Created by zhanyage on 2019-12-02
 * Describe:
 */
public class VideoSpan extends ImageSpan {

    /**
     * 视频的 url
     */
    private String resourceUrl;

    public VideoSpan(Drawable d, String source) {
        super(d, source, 0);
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

}
