package com.zhanyage.htmlparse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.zhanyage.htmlparse.util.ScreenUtils
import com.zhanyage.htmlparselib.ClickMovementMethod

class MainActivity : AppCompatActivity() {

    private lateinit var tvMainTest: TextView
    private lateinit var btnMainRefresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ScreenUtils.init(this)
        tvMainTest = findViewById(R.id.tv_main_test)
        btnMainRefresh = findViewById(R.id.btn_main_refresh)

        val source =
            "<p>这是一个测试</p><p><b>加粗1</b></p><p><i>斜体new</i></p><p><i><u>下划线</u></i><i style=\"color: rgb(229, 51, 51);\"><u>文字颜色阿萨德</u></i></p><ol><li><i style=\"color: rgb(229, 51, 51);\"><u>列表11</u></i></li><li><i style=\"color: rgb(229, 51, 51);\"><u>列表211</u></i></li></ol><ul><li><i style=\"color: rgb(229, 51, 51);\"><u>列表31212nrew</u></i></li><li><i style=\"color: rgb(229, 51, 51);\"><u>列表4</u></i></li></ul><p><span  style=\"font-size: 32px; color: rgb(0, 0, 0);\">文字大小姐姐</span></p><ol><li><b style=\"color: rgb(0, 102, 0);\"><i><u><a target=\"_blank\" href=\"http://www.baidu.com\"><span  style=\"font-size: 32px;\">混合</span></a></u></i></b></li></ol><p><a target=\"_blank\" href=\"http://www.baidu.com\" style=\"color: rgb(51, 51, 51);\"><span  style=\"font-size: 32px;\">普通链接</span></a></p><p><br></p><p><a target=\"_blank\" href=\"qiyu://action.qiyukf.com?command=applyHumanStaff\">转人工</a></p><p>[呕吐]1</p><p><img height=\"200\" width=\"200\" title=\"image title\" src=\"https://bot-resource-public.nos-hz.163yun.com/354d31e2-d990-4b1d-947e-31223ac3a7b4.jpg\"></p><p><img height=\"200\" width=\"200\" title=\"image title\" src=\"https://bot-resource-public.nos-hz.163yun.com/6a26bd98-0806-4f09-8e4c-17e1374d4ee5.gif\"></p>"
        btnMainRefresh.setOnClickListener {
            HtmlEx.displayHtmlText(tvMainTest, source, ScreenUtils.dp2px(300f))
        }
        tvMainTest.setOnTouchListener(ClickMovementMethod.newInstance())
    }
}
