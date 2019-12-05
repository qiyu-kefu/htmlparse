/*
 * Copyright (C) 2013-2015 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (C) 2013-2015 Juha Kuitunen
 * Copyright (C) 2013 Mohammed Lakkadshaw
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhanyage.htmlparselib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.TextView;


import com.zhanyage.htmlparselib.span.NumberSpan;
import com.zhanyage.htmlparselib.span.VideoSpan;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * custom html tagHandler，解析 html 的核心内容都在这里
 */
class HtmlTagHandler implements Html.TagHandler {

    public static final String VIDEO_DEFAULT_IMG_TAG = "defaultImg";

    private static final String UNORDERED_LIST = "HTML_TEXT_TAG_UL";
    private static final String ORDERED_LIST = "HTML_TEXT_TAG_OL";
    private static final String LIST_ITEM = "HTML_TEXT_TAG_LI";
    private static final String FONT = "HTML_TEXT_TAG_FONT";
    private static final String DIV = "HTML_TEXT_TAG_DIV";
    private static final String SPAN = "HTML_SPAN_STYLE";
    private static final String BSTYLE = "HTML_B_STYLE";
    private static final String PSTYLE = "HTML_P_STYLE";
    private static final String ASTYLE = "HTML_A_STYLE";
    private static final String USTYLE = "HTML_U_STYLE";
    private static final String ISTYLE = "HTML_I_STYLE";
    private static final String IMGLABEL = "HTML_IMG_LABEL";
    private static final String VIDEOlABEL = "HTML_VIDEO_LABEL";
    private static final String BRLABEL = "HTML_BR_LABEL";

    private static Pattern sForegroundColorPattern;
    private static Pattern sForegroundFontSizePattern;
    private static Pattern sRgbColorPattern;
    private static Pattern sArgbColorPattern;
    private static Pattern sHexColorPattern;
    private static Pattern sTextAlignPattern;
    private static final Map<String, Integer> sColorMap;

    private Context mContext;
    private TextPaint mTextPaint;
    private Html.ImageGetter imageLoader;

    /**
     * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
     * and on top of Stack is the most nested list
     */
    private Stack<String> lists = new Stack<>();
    /**
     * Tracks indexes of ordered lists so that after a nested list ends
     * we can continue with correct index of outer list
     */
    private Stack<Integer> olNextIndex = new Stack<>();

    private static final int indent = 10;
    private static final int listItemIndent = indent * 2;
    private static final BulletSpan bullet = new BulletSpan(indent);

    void setTextView(TextView textView) {
        mContext = textView.getContext().getApplicationContext();
        mTextPaint = textView.getPaint();
    }

    void setImageGetter(Html.ImageGetter imageLoader) {
        this.imageLoader = imageLoader;
    }

    /*
     * Newer versions of the Android SDK's {@link Html.TagHandler} handles &lt;ul&gt; and &lt;li&gt;
     * tags itself which means they never get delegated to this class. We want to handle the tags
     * ourselves so before passing the string html into Html.fromHtml(), we can use this method to
     * replace the &lt;ul&gt; and &lt;li&gt; tags with tags of our own.
     *
     * @param html String containing HTML, for example: "<b>Hello world!</b>"
     * @return html with replaced <ul> and <li> tags
     * @see <a href="https://github.com/android/platform_frameworks_base/commit/8b36c0bbd1503c61c111feac939193c47f812190">Specific Android SDK Commit</a>
     */
    static {
        sColorMap = new HashMap<>();
        sColorMap.put("darkgray", 0xFFA9A9A9);
        sColorMap.put("gray", 0xFF808080);
        sColorMap.put("lightgray", 0xFFD3D3D3);
        sColorMap.put("darkgrey", 0xFFA9A9A9);
        sColorMap.put("grey", 0xFF808080);
        sColorMap.put("lightgrey", 0xFFD3D3D3);
        sColorMap.put("green", 0xFF008000);
    }


    String overrideTags(String html) {
        if (html == null) {
            return null;
        }

        // Remove useless div tags
        html = html.replaceAll("<div>(<img\\s+[^>]*>)</div>", "$1");

        // Wrap HTML tags to prevent parsing custom tags error
        html = "<html>" + html + "</html>";

        html = html.replace("<ul", "<" + UNORDERED_LIST);
        html = html.replace("</ul>", "</" + UNORDERED_LIST + ">");
        html = html.replace("<ol", "<" + ORDERED_LIST);
        html = html.replace("</ol>", "</" + ORDERED_LIST + ">");
        html = html.replace("<li", "<" + LIST_ITEM);
        html = html.replace("</li>", "</" + LIST_ITEM + ">");
        html = html.replace("<font", "<" + FONT);
        html = html.replace("</font>", "</" + FONT + ">");
        html = html.replace("<div", "<" + DIV);
        html = html.replace("</div>", "</" + DIV + ">");
        html = html.replace("<span", "<" + SPAN);
        html = html.replace("</span>", "</" + SPAN + ">");

        //这个地方是防止 <img 标签被错误替换了
        html = html.replace("<br", "<" + BRLABEL);
        html = html.replace("<b", "<" + BSTYLE);
        html = html.replace("<" + BRLABEL, "<br");

        html = html.replace("</b>", "</" + BSTYLE + ">");
        html = html.replace("<p", "<" + PSTYLE);
        html = html.replace("</p>", "</" + PSTYLE + ">");
        html = html.replace("<a", "<" + ASTYLE);
        html = html.replace("</a>", "</" + ASTYLE + ">");
        html = html.replace("<u", "<" + USTYLE);
        html = html.replace("</u>", "</" + USTYLE + ">");

        //这个地方是防止 <img 标签被错误替换了
        html = html.replace("<img", "<" + IMGLABEL);
        html = html.replace("<i", "<" + ISTYLE);
        html = html.replace("<" + IMGLABEL, "<img");
        html = html.replace("<video", "<" + VIDEOlABEL);

        html = html.replace("</i>", "</" + ISTYLE + ">");
        html = html.replace("\n", "<br>");

        return html;
    }

    @Override
    public void handleTag(final boolean opening, final String tag, Editable output, final XMLReader xmlReader) {
        if (opening) {
            // opening tag
            if (tag.equalsIgnoreCase(UNORDERED_LIST)) {
                lists.push(tag);
            } else if (tag.equalsIgnoreCase(ORDERED_LIST)) {
                lists.push(tag);
                olNextIndex.push(1);
            } else if (tag.equalsIgnoreCase(LIST_ITEM)) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                    output.append("\n");
                }
                if (!lists.isEmpty()) {
                    String parentList = lists.peek();
                    if (parentList.equalsIgnoreCase(ORDERED_LIST)) {
                        start(output, new Ol());
                        olNextIndex.push(olNextIndex.pop() + 1);
                    } else if (parentList.equalsIgnoreCase(UNORDERED_LIST)) {
                        start(output, new Ul());
                    }
                }
            } else if (tag.equalsIgnoreCase(FONT)) {
                startFont(output, xmlReader);
            } else if (tag.equalsIgnoreCase(DIV)) {
                handleDiv(output);
            } else if (tag.equalsIgnoreCase("code")) {
                start(output, new Code());
            } else if (tag.equalsIgnoreCase("center")) {
                start(output, new Center());
            } else if (tag.equalsIgnoreCase("s") || tag.equalsIgnoreCase("strike")) {
                start(output, new Strike());
            } else if (tag.equalsIgnoreCase("tr")) {
                start(output, new Tr());
            } else if (tag.equalsIgnoreCase("th")) {
                start(output, new Th());
            } else if (tag.equalsIgnoreCase("td")) {
                start(output, new Td());
            } else if (tag.equalsIgnoreCase(SPAN)) {
                startSpan(output, xmlReader);
            } else if (tag.equalsIgnoreCase(BSTYLE)) {
                startForNative(output, new Bold());
                startSpan(output, xmlReader);
            } else if (tag.equalsIgnoreCase(PSTYLE)) {
                startBlockElement(output, xmlReader, getMarginParagraph());
                startSpan(output, xmlReader);
            } else if (tag.equalsIgnoreCase(ASTYLE)) {
                startA(output, xmlReader);
                startSpan(output, xmlReader);
            } else if (tag.equalsIgnoreCase(USTYLE)) {
                startForNative(output, new Underline());
                startSpan(output, xmlReader);
            } else if (tag.equalsIgnoreCase(ISTYLE)) {
                startForNative(output, new Italic());
                startSpan(output, xmlReader);
            } else if (tag.equalsIgnoreCase(VIDEOlABEL)) {
                startVideo(output, xmlReader, imageLoader);
            }
        } else {
            // closing tag
            if (tag.equalsIgnoreCase(UNORDERED_LIST)) {
                lists.pop();
            } else if (tag.equalsIgnoreCase(ORDERED_LIST)) {
                lists.pop();
                olNextIndex.pop();
            } else if (tag.equalsIgnoreCase(LIST_ITEM)) {
                if (!lists.isEmpty()) {
                    if (lists.peek().equalsIgnoreCase(UNORDERED_LIST)) {
                        if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                            output.append("\n");
                        }
                        // Nested BulletSpans increases distance between bullet and text, so we must prevent it.
                        int bulletMargin = indent;
                        if (lists.size() > 1) {
                            bulletMargin = indent - bullet.getLeadingMargin(true);
                            if (lists.size() > 2) {
                                // This get's more complicated when we add a LeadingMarginSpan into the same line:
                                // we have also counter it's effect to BulletSpan
                                bulletMargin -= (lists.size() - 2) * listItemIndent;
                            }
                        }
                        BulletSpan newBullet = new BulletSpan(bulletMargin);
                        end(output, Ul.class, false,
                                new LeadingMarginSpan.Standard(listItemIndent * (lists.size() - 1)),
                                newBullet);
                    } else if (lists.peek().equalsIgnoreCase(ORDERED_LIST)) {
                        if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                            output.append("\n");
                        }
                        int numberMargin = listItemIndent * (lists.size() - 1);
                        if (lists.size() > 2) {
                            // Same as in ordered lists: counter the effect of nested Spans
                            numberMargin -= (lists.size() - 2) * listItemIndent;
                        }
                        NumberSpan numberSpan = new NumberSpan(mTextPaint, olNextIndex.lastElement() - 1);
                        end(output, Ol.class, false,
                                new LeadingMarginSpan.Standard(numberMargin),
                                numberSpan);
                    }
                }
            } else if (tag.equalsIgnoreCase(FONT)) {
                endFont(output);
            } else if (tag.equalsIgnoreCase(DIV)) {
                handleDiv(output);
            } else if (tag.equalsIgnoreCase("code")) {
                end(output, Code.class, false, new TypefaceSpan("monospace"));
            } else if (tag.equalsIgnoreCase("center")) {
                end(output, Center.class, true, new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER));
            } else if (tag.equalsIgnoreCase("s") || tag.equalsIgnoreCase("strike")) {
                end(output, Strike.class, false, new StrikethroughSpan());
            } else if (tag.equalsIgnoreCase("tr")) {
                end(output, Tr.class, false);
            } else if (tag.equalsIgnoreCase("th")) {
                end(output, Th.class, false);
            } else if (tag.equalsIgnoreCase("td")) {
                end(output, Td.class, false);
            } else if (tag.equalsIgnoreCase(SPAN)) {
                endSpan(output);
            } else if (tag.equalsIgnoreCase(BSTYLE)) {
                endSpan(output);
                endForNative(output, Bold.class, new StyleSpan(Typeface.BOLD));
            } else if (tag.equalsIgnoreCase(PSTYLE)) {
                endSpan(output);
                endBlockElement(output);
            } else if (tag.equalsIgnoreCase(ASTYLE)) {
                endSpan(output);
                endA(output);
            } else if (tag.equalsIgnoreCase(USTYLE)) {
                endSpan(output);
                endForNative(output, Underline.class, new UnderlineSpan());
            } else if (tag.equalsIgnoreCase(ISTYLE)) {
                endSpan(output);
                endForNative(output, Italic.class, new StyleSpan(Typeface.ITALIC));
            }
        }
    }

    private void startSpan(Editable output, XMLReader xmlReader) {
        Map<String, String> attributes = getAttributes(xmlReader);
        String style = attributes.get("style");
        if (style != null) {
            Foreground object = new Foreground();
            Matcher mColor = getForegroundColorPattern().matcher(style);
            if (mColor.find()) {
                int c = getHtmlColor(mColor.group(1));
                if (c != -1) {
//                    int len = output.length();
                    object.setmForegroundColor(c);
//                    output.setSpan(new Foreground(c), len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            Matcher mSize = getsForegroundFontSizePattern().matcher(style);
            if (mSize.find()) {
                try {
                    int s = Integer.parseInt(mSize.group(1));
                    object.setmForegroundSize(s);
                } catch (NumberFormatException e) {
                    //log
                    Log.e("HtmlTagHandler", "start span is error", e);
                }
            }
            int len = output.length();
            output.setSpan(object, len, len, Spannable.SPAN_MARK_MARK);
        }
    }


    private void endSpan(Editable output) {
        int len = output.length();
        Object obj = getSpanLast(output, Foreground.class);
        int where = output.getSpanStart(obj);

        output.removeSpan(obj);

        if (where != len) {
            Foreground f = (Foreground) obj;
            if (f == null)
                return;
            int color = f.getmForegroundColor();
            int size = f.mForegroundSize;

            if (color != -1) {
                output.setSpan(new ForegroundColorSpan(color | 0xFF000000), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (size > 0) {
                output.setSpan(new AbsoluteSizeSpan(size, true), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void startVideo(Editable text, XMLReader xmlReader, Html.ImageGetter img) {
        Map<String, String> attributes = getAttributes(xmlReader);
        String videoUrl = attributes.get("src");
        String imgUrl = attributes.get("poster");
        Drawable d = null;

        if (img != null) {
            if (TextUtils.isEmpty(imgUrl)) {
                d = img.getDrawable("VIDEO_IMG_TAG" + VIDEO_DEFAULT_IMG_TAG);
            } else {
                d = img.getDrawable("VIDEO_IMG_TAG" + imgUrl);
            }
        }

        if (d == null) {
            d = img.getDrawable("VIDEO_IMG_TAG" + VIDEO_DEFAULT_IMG_TAG);
            if (d != null)
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }

        int len = text.length();
        text.append("\uFFFC");

        VideoSpan imageAndVideoSpan = new VideoSpan(d, imgUrl);
        imageAndVideoSpan.setResourceUrl(videoUrl);

        text.setSpan(imageAndVideoSpan, len, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static class Ul {
    }

    private static class Ol {
    }

    private static class Code {
    }

    private static class Center {
    }

    private static class Strike {
    }

    private static class Tr {
    }

    private static class Th {
    }

    private static class Td {
    }

    private static class Bold {
    }

    private static class Underline {
    }

    private static class Italic {
    }

    private static class Newline {
        private int mNumNewlines;

        public Newline(int numNewlines) {
            mNumNewlines = numNewlines;
        }
    }

    private static class Alignment {
        private Layout.Alignment mAlignment;

        public Alignment(Layout.Alignment alignment) {
            mAlignment = alignment;
        }
    }

    private static class Href {
        public String mHref;

        public Href(String href) {
            mHref = href;
        }
    }

    private static class Foreground {
        private int mForegroundColor = 0;
        private int mForegroundSize = 0;

        public int getmForegroundColor() {
            return mForegroundColor;
        }

        public void setmForegroundColor(int mForegroundColor) {
            this.mForegroundColor = mForegroundColor;
        }

        public int getmForegroundSize() {
            return mForegroundSize;
        }

        public void setmForegroundSize(int mForegroundSize) {
            this.mForegroundSize = mForegroundSize;
        }
    }

    private static class Font {
        public String color;
        public String size;

        public Font(String color, String size) {
            this.color = color;
            this.size = size;
        }
    }

    //html 源码中的方法
    private static void startA(Editable text, XMLReader xmlReader) {
        Map<String, String> attributes = getAttributes(xmlReader);
        String href = attributes.get("href");
        Log.e("htmlTagHandler:", href);
        startForNative(text, new Href(href));
    }

    //html 源码中的方法
    private static void endA(Editable text) {
        Href h = getLastForNative(text, Href.class);
        if (h != null) {
            if (h.mHref != null) {
                setSpanFromMark(text, h, new URLSpan((h.mHref)));
            }
        }
    }

    //Html 源码中的 strat 方法
    private static void startForNative(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    //html 源码中的 end 方法
    private static void endForNative(Editable text, Class kind, Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        if (obj != null) {
            setSpanFromMark(text, obj, repl);
        }
    }

    //Html 源码中的方法
    private static void startBlockElement(Editable text, XMLReader xmlReader, int margin) {
        Map<String, String> attributes = getAttributes(xmlReader);
        String style = attributes.get("style");
        final int len = text.length();
        if (margin > 0) {
            appendNewlines(text, margin);
            startForNative(text, new Newline(margin));
        }

        if (style != null) {
            Matcher m = getTextAlignPattern().matcher(style);
            if (m.find()) {
                String alignment = m.group(1);
                if (alignment.equalsIgnoreCase("start")) {
                    startForNative(text, new Alignment(Layout.Alignment.ALIGN_NORMAL));
                } else if (alignment.equalsIgnoreCase("center")) {
                    startForNative(text, new Alignment(Layout.Alignment.ALIGN_CENTER));
                } else if (alignment.equalsIgnoreCase("end")) {
                    startForNative(text, new Alignment(Layout.Alignment.ALIGN_OPPOSITE));
                }
            }
        }
    }

    private static Pattern getTextAlignPattern() {
        if (sTextAlignPattern == null) {
            sTextAlignPattern = Pattern.compile("(?:\\s+|\\A)text-align\\s*:\\s*(\\S*)\\b");
        }
        return sTextAlignPattern;
    }

    private static void appendNewlines(Editable text, int minNewline) {
        final int len = text.length();

        if (len == 0) {
            return;
        }

        int existingNewlines = 0;
        for (int i = len - 1; i >= 0 && text.charAt(i) == '\n'; i--) {
            existingNewlines++;
        }

        for (int j = existingNewlines; j < minNewline; j++) {
            text.append("\n");
        }
    }

    //html 源码中的方法
    private static void endBlockElement(Editable text) {
        Newline n = getLastForNative(text, Newline.class);
        if (n != null) {
            appendNewlines(text, n.mNumNewlines);
            text.removeSpan(n);
        }

        Alignment a = getLastForNative(text, Alignment.class);
        if (a != null) {
            setSpanFromMark(text, a, new AlignmentSpan.Standard(a.mAlignment));
        }
    }

    /**
     * Mark the opening tag by using private classes
     */
    private void start(Editable output, Object mark) {
        int len = output.length();
        output.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    /**
     * Modified from {@link Html}
     */
    private void end(Editable output, Class kind, boolean paragraphStyle, Object... replaces) {
        Object obj = getLast(output, kind);
        // start of the tag
        int where = output.getSpanStart(obj);
        // end of the tag
        int len = output.length();

        output.removeSpan(obj);

        if (where != len) {
            int thisLen = len;
            // paragraph styles like AlignmentSpan need to end with a new line!
            if (paragraphStyle) {
                output.append("\n");
                thisLen++;
            }
            for (Object replace : replaces) {
                output.setSpan(replace, where, thisLen, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void startFont(Editable output, XMLReader xmlReader) {
        int len = output.length();
        Map<String, String> attributes = getAttributes(xmlReader);
        String color = attributes.get("color");
        String size = attributes.get("size");
        output.setSpan(new Font(color, size), len, len, Spannable.SPAN_MARK_MARK);
    }

    private void endFont(Editable output) {
        int len = output.length();
        Object obj = getLast(output, Font.class);
        int where = output.getSpanStart(obj);

        output.removeSpan(obj);

        if (where != len) {
            Font f = (Font) obj;
            int color = parseColor(f.color);
            int size = parseSize(f.size);

            if (color != -1) {
                output.setSpan(new ForegroundColorSpan(color | 0xFF000000), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (size > 0) {
                output.setSpan(new AbsoluteSizeSpan(size, true), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void handleDiv(Editable output) {
        int len = output.length();

        if (len >= 1 && output.charAt(len - 1) == '\n') {
            return;
        }

        if (len != 0) {
            output.append("\n");
        }
    }

    private static HashMap<String, String> getAttributes(XMLReader xmlReader) {
        HashMap<String, String> attributes = new HashMap<>();
        try {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);
            Field attrsField = element.getClass().getDeclaredField("theAtts");
            attrsField.setAccessible(true);
            Object attrs = attrsField.get(element);
            Field dataField = attrs.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            String[] data = (String[]) dataField.get(attrs);
            Field lengthField = attrs.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            int len = (Integer) lengthField.get(attrs);

            /**
             * MSH: Look for supported attributes and add to hash map.
             * This is as tight as things can get :)
             * The data index is "just" where the keys and values are stored.
             */
            for (int i = 0; i < len; i++) {
                attributes.put(data[i * 5 + 1], data[i * 5 + 4]);
            }
        } catch (Exception ignored) {
        }
        return attributes;
    }

    //html 源码中的方法
    private static <T> T getLastForNative(Spanned text, Class<T> kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        T[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    //html 源码中的方法
    private static void setSpanFromMark(Spannable text, Object mark, Object... spans) {
        int where = text.getSpanStart(mark);
        text.removeSpan(mark);
        int len = text.length();
        if (where != len) {
            for (Object span : spans) {
                text.setSpan(span, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * Get last marked position of a specific tag kind (private class)
     */
    private static Object getLast(Editable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        } else {
            for (int i = objs.length; i > 0; i--) {
                if (text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK) {
                    return objs[i - 1];
                }
            }
            return null;
        }
    }

    private static int parseColor(String colorString) {
        try {
            return Color.parseColor(colorString);
        } catch (Exception ignored) {
            return -1;
        }
    }

    /**
     * dpValue
     */
    private int parseSize(String size) {
        int s;
        try {
            s = Integer.parseInt(size);
        } catch (NumberFormatException ignored) {
            return 0;
        }

        s = Math.max(s, 1);
        s = Math.min(s, 7);

        int baseSize = px2dp(mTextPaint.getTextSize());

        return (s - 3) + baseSize;
    }

    private int px2dp(float pxValue) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    private static <T> T getSpanLast(Spanned text, Class<T> kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        T[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private int getHtmlColor(String color) {
        // 16进制颜色值
        try {
            Matcher hexMatcher = getHexColorPattern().matcher(color);
            if (hexMatcher.find()) {
                String hexColor = hexMatcher.group(1);
                return Color.parseColor(hexColor);
            }
        } catch (Exception ignore) {
        }
        // rgb进制颜色值
        try {
            Matcher rgbMatcher = getRgbColorPattern().matcher(color);
            if (rgbMatcher.find()) {
                int r = Integer.valueOf(rgbMatcher.group(1));
                int g = Integer.valueOf(rgbMatcher.group(2));
                int b = Integer.valueOf(rgbMatcher.group(3));
                return Color.rgb(r, g, b);
            }
        } catch (Exception ignore) {
        }
        // argb颜色值
        try {
            Matcher argbMatcher = getArgbColorPattern().matcher(color);
            if (argbMatcher.find()) {
                int r = Integer.valueOf(argbMatcher.group(1));
                int g = Integer.valueOf(argbMatcher.group(2));
                int b = Integer.valueOf(argbMatcher.group(3));
                float a = Float.valueOf(argbMatcher.group(4));
                return Color.argb((int) (a * 255), r, g, b);
            }
        } catch (Exception ignore) {
        }

        if ((0x00000000 & Html.FROM_HTML_OPTION_USE_CSS_COLORS)
                == Html.FROM_HTML_OPTION_USE_CSS_COLORS) {
            Integer i = sColorMap.get(color.toLowerCase(Locale.US));
            if (i != null) {
                return i;
            }
        }
        return Color.BLACK;
    }

    private static Pattern getArgbColorPattern() {
        if (sArgbColorPattern == null) {
            sArgbColorPattern = Pattern.compile("^\\s*rgba\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*([\\d.]+)\\b");
        }
        return sArgbColorPattern;
    }

    private static Pattern getHexColorPattern() {
        if (sHexColorPattern == null) {
            sHexColorPattern = Pattern.compile("^\\s*(#[A-Za-z0-9]{6,8})");
        }
        return sHexColorPattern;
    }

    private static Pattern getRgbColorPattern() {
        if (sRgbColorPattern == null) {
            sRgbColorPattern = Pattern.compile("^\\s*rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\b");
        }
        return sRgbColorPattern;
    }

    private static Pattern getForegroundColorPattern() {
        if (sForegroundColorPattern == null) {
            sForegroundColorPattern = Pattern.compile(
                    "(?:\\s+|\\A|;\\s*)color\\s*:\\s*(.*)\\b");
        }
        return sForegroundColorPattern;
    }

    private static Pattern getsForegroundFontSizePattern() {
        if (sForegroundFontSizePattern == null) {
            sForegroundFontSizePattern = Pattern.compile("font-size: (\\d+)px");
        }
        return sForegroundFontSizePattern;
    }

    private int getMarginParagraph() {
        return getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH);
    }


    /**
     * Returns the minimum number of newline characters needed before and after a given block-level
     * element.
     * 源码中的方法
     *
     * @param flag the corresponding option flag defined in {@link Html} of a block-level element
     */
    private int getMargin(int flag) {
        if ((flag & 0x00000000) != 0) {
            return 1;
        }
        return 1;
    }
}
