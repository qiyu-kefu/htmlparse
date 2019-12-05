package com.zhanyage.htmlparselib;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.widget.TextView;


import com.zhanyage.htmlparselib.api.HtmlImageLoader;
import com.zhanyage.htmlparselib.api.OnTagClickListener;
import com.zhanyage.htmlparselib.span.ImageClickSpan;
import com.zhanyage.htmlparselib.span.LinkClickSpan;
import com.zhanyage.htmlparselib.span.VideoClickSpan;
import com.zhanyage.htmlparselib.span.VideoSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Html config builder
 */
public class HtmlText {
    private HtmlImageLoader imageLoader;
    private OnTagClickListener onTagClickListener;
    private After after;
    private String source;

    /**
     * TagHandler 解析完成后对 html 的处理 After
     */
    public interface After {
        CharSequence after(SpannableStringBuilder ssb);
    }

    private HtmlText(String source) {
        this.source = source;
    }

    public static HtmlText from(String source) {
        return new HtmlText(source);
    }

    public HtmlText setImageLoader(HtmlImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    public HtmlText setOnTagClickListener(OnTagClickListener onTagClickListener) {
        this.onTagClickListener = onTagClickListener;
        return this;
    }

    public HtmlText after(After after) {
        this.after = after;
        return this;
    }

    public void into(TextView textView) {
        if (TextUtils.isEmpty(source)) {
            textView.setText("");
            return;
        }

        HtmlImageGetter imageGetter = new HtmlImageGetter();
        HtmlTagHandler tagHandler = new HtmlTagHandler();
        List<String> imageUrls = new ArrayList<>();

        imageGetter.setTextView(textView);
        imageGetter.setImageLoader(imageLoader);
        imageGetter.getImageSize(source);

        tagHandler.setTextView(textView);
        tagHandler.setImageGetter(imageGetter);
        source = tagHandler.overrideTags(source);

        Spanned spanned = Html.fromHtml(source, imageGetter, tagHandler);
        SpannableStringBuilder ssb;
        if (spanned instanceof SpannableStringBuilder) {
            ssb = (SpannableStringBuilder) spanned;
        } else {
            ssb = new SpannableStringBuilder(spanned);
        }

        //Hold image url link
        imageUrls.clear();
        ImageSpan[] imageSpans = ssb.getSpans(0, ssb.length(), ImageSpan.class);
        for (int i = 0; i < imageSpans.length; i++) {
            ImageSpan imageSpan = imageSpans[i];
            String imageUrl = imageSpan.getSource();
            int start = ssb.getSpanStart(imageSpan);
            int end = ssb.getSpanEnd(imageSpan);
            imageUrls.add(imageUrl);

            ImageClickSpan imageClickSpan = new ImageClickSpan(textView.getContext(), imageUrls, i);
            imageClickSpan.setListener(onTagClickListener);
            ClickableSpan[] clickableSpans = ssb.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null) {
                for (ClickableSpan cs : clickableSpans) {
                    ssb.removeSpan(cs);
                }
            }
            ssb.setSpan(imageClickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        //Hold video url link
        VideoSpan[] videoSpans = ssb.getSpans(0, ssb.length(), VideoSpan.class);
        for (VideoSpan videoSpan : videoSpans) {
            int start = ssb.getSpanStart(videoSpan);
            int end = ssb.getSpanEnd(videoSpan);

            VideoClickSpan videoClickSpan = new VideoClickSpan(textView.getContext(), videoSpan.getResourceUrl());
            videoClickSpan.setListener(onTagClickListener);
            ClickableSpan[] clickableSpans = ssb.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null) {
                for (ClickableSpan cs : clickableSpans) {
                    ssb.removeSpan(cs);
                }
            }
            ssb.setSpan(videoClickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Hold text url link
        URLSpan[] urlSpans = ssb.getSpans(0, ssb.length(), URLSpan.class);
        if (urlSpans != null) {
            for (URLSpan urlSpan : urlSpans) {
                int start = ssb.getSpanStart(urlSpan);
                int end = ssb.getSpanEnd(urlSpan);
                ssb.removeSpan(urlSpan);
                LinkClickSpan linkClickSpan = new LinkClickSpan(textView.getContext(), urlSpan.getURL());
                linkClickSpan.setListener(onTagClickListener);
                ssb.setSpan(linkClickSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }

        // Remove blank lines
        while (ssb.length() > 0 && ssb.charAt(ssb.length() - 1) == '\n') {
            ssb.delete(ssb.length() - 1, ssb.length());
        }

        //交给 after 处理
        CharSequence charSequence = ssb;
        if (after != null) {
            charSequence = after.after(ssb);
        }

        textView.setText(charSequence);
    }
}
