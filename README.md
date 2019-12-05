# htmlparse

概述：

七鱼 html 代码解析工程，主要用于把 String 类型的 Html 代码展示在 view 上面

## 功能介绍

把 String 类型的 html 代码展示在 view 上面，并 html 的标签的样式不会丢失

目前已经支持的 html 标签有：

- ```<ul>```
- ```<ol>```
- ```<li>```
- ```<font>```
- ```<div>```
- ```<span>```
- ```<br>```
- ```<b>```
- ```<p>```
- ```<a>```
- ```<u>```
- ```<img>```
- ```<i>```
- ```<video>```

## 使用方法

```
HtmlText.from(source) //source 为 String 类型的 html 代码
	    .setImageLoader(mImageLoader) //mImagerLoader 为加载图片回调的方法
	    .setOnTagClickListener(mTagClickListener) //mTagClickListener 为点击事件的回调
	    .after(mAfter) 
	    .into(mTextView); //想要展示信息的 textView
```

具体使用细节请查看 example module