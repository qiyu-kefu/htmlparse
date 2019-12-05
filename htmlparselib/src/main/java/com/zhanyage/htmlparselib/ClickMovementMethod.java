package com.zhanyage.htmlparselib;

import android.text.Layout;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.TextView;

/**
 * html view OnTouchListener
 * 如果想让 view 支持 html 的点击事件，必须设置 view 的 OnTouchListener 为该类
 */
public class ClickMovementMethod implements View.OnTouchListener {
    private LongClickCallback longClickCallback;
    private boolean isEventStart;

    public static ClickMovementMethod newInstance() {
        return new ClickMovementMethod();
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        if (longClickCallback == null) {
            longClickCallback = new LongClickCallback(v);
        }

        TextView widget = (TextView) v;
        // MovementMethod设为空，防止消费长按事件
        widget.setMovementMethod(null);
        CharSequence text = widget.getText();
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(text);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (widget == null) {
                return false;
            }
            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();
            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);
            ClickableSpan[] link = spannable.getSpans(off, off, ClickableSpan.class);
            if (link.length != 0) {
                if (action == MotionEvent.ACTION_DOWN) {
                    isEventStart = true;
                    v.postDelayed(longClickCallback, ViewConfiguration.getLongPressTimeout());
                } else if (isEventStart) {
                    v.removeCallbacks(longClickCallback);
                    if (action == MotionEvent.ACTION_UP) {
                        onSpanClick(link[0], widget);
                    }
                }
                return true;
            }
        }

        return false;
    }

    private void onSpanClick(ClickableSpan link, View widget) {
        try {
            link.onClick(widget);
        } catch (Throwable ignored) {
            // may throw ActivityNotFoundException
            Log.e("onSpanClick is error", "", ignored);
        }
    }

    private class LongClickCallback implements Runnable {
        private View view;

        LongClickCallback(View view) {
            this.view = view;
        }

        @Override
        public void run() {
            // 找到能够消费长按事件的View
            View v = view;
            if (v == null) {
                return;
            }
            try {
                boolean consumed = v.performLongClick();
                while (!consumed) {
                    ViewParent parent = v.getParent();
                    if (!(parent instanceof View)) {
                        break;
                    }
                    v = (View) v.getParent();
                    consumed = v.performLongClick();
                }

                isEventStart = false;
            } catch (NullPointerException e) {
                Log.e("ClickMovementMethod", "LongClickCallback callback is exception", e);
            }

        }
    }
}
