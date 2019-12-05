package com.zhanyage.htmlparse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zhanyage.htmlparse.util.BitmapUtil;
import com.zhanyage.htmlparselib.api.HtmlImageLoader;
import com.zhanyage.htmlparselib.HtmlText;
import com.zhanyage.htmlparselib.api.OnTagClickListener;

import java.util.List;


/**
 * example for html parse
 */
public class HtmlEx {

    public static void displayHtmlText(final TextView textView, String source, final int maxSize) {
        if (textView == null || source == null || maxSize == 0) {
            return;
        }
        final Context context = textView.getContext();
        final HtmlText htmlText = HtmlText.from(source)
                .setImageLoader(new HtmlImageLoader() {
                    @Override
                    public void loadImage(String url, final Callback callback) {
                        Glide.with(context).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                Log.e("onLoadFailed", "is error");
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition transition) {
                                callback.onLoadComplete(resource);
                            }
                        });
                    }

                    @Override
                    public void loadVideoImage(String url, final Callback callback) {
                        if (context == null) {
                            return;
                        }

                        //当是默认图片的时候，需要加载默认图片
                        if ("defaultImg".equals(url) || TextUtils.isEmpty(url)) {
                            callback.onLoadComplete(loadDefaultVideoBitmap(context));
                        } else {

                            Glide.with(context).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    Log.e("onLoadFailed", "is error");
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }

                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition transition) {
                                    Bitmap videoBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_video_play_icon);
                                    Bitmap resultBitmap = BitmapUtil.combineBitmap(resource, videoBitmap);
                                    callback.onLoadComplete(resultBitmap);
                                }
                            });
                        }
                    }

                    @Override
                    public Drawable getDefaultDrawable() {
                        //获取默认图片
                        return ContextCompat.getDrawable(context, R.mipmap.ic_image_placeholder_loading);
                    }

                    @Override
                    public Drawable getErrorDrawable() {
                        //获取加载错误的图片
                        return ContextCompat.getDrawable(context, R.mipmap.ic_image_placeholder_fail);
                    }

                    @Override
                    public int getMaxWidth() {
                        return maxSize;
                    }

                    @Override
                    public boolean fitWidth() {
                        return false;
                    }
                })
                .after(new HtmlText.After() {
                    @Override
                    public CharSequence after(SpannableStringBuilder ssb) {
                        //这个地方可以在做一些其他的处理，例如把 html 中的文字转换称表情等，具体实现可以使用正则表达式去匹配等
                        return ssb;
                    }
                });

        htmlText.setOnTagClickListener(new OnTagClickListener() {
            @Override
            public void onImageClick(Context context, List<String> imageUrlList, int position) {
                //查看图片的点击事件
                Toast.makeText(context, "点击图片链接 url:" + imageUrlList.get(0), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLinkClick(Context context, String url) {
                //url 的点击事件
                Toast.makeText(context, "点击超链接 url:" + url, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVideoClick(Context context, String videoUrl) {
                //查看视频的点击事件
                Toast.makeText(context, "播放视频的 url:" + videoUrl, Toast.LENGTH_LONG).show();
            }
        });

        htmlText.into(textView);
    }

    private static Bitmap loadDefaultVideoBitmap(Context context) {
        Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_default_video_img);
        Bitmap videoBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_video_play_icon);
        //两个 bitmap 合成一个
        return BitmapUtil.combineBitmap(defaultBitmap, videoBitmap);
    }
}
