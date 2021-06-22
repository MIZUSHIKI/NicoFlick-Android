package com.mizushiki.nicoflick_a


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_wikipageweb.*
import java.net.URLDecoder

class Activity_WikiPageWeb : AppCompatActivity() {

    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    val texts = arrayListOf<String>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wikipageweb)

        setSupportActionBar(toolbar2)
        getSupportActionBar()!!.setTitle("NicoFlick")
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://main-timetag.ssl-lolipop.jp/nicoflick/wiki/index.php")
        webView.webViewClient = WebViewClient()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    if( it.startsWith("nicoflick://") ){
                        webView.isVisible = false
                        // インテントのインスタンス作成
                        val data = Intent()
                        // インテントに値をセット
                        data.putExtra("modori", it)
                        // 結果を設定
                        setResult(RESULT_OK, data)
                        finish()
                        return false
                    }
                }
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                webView.evaluateJavascript("document.body.innerHTML"){
                    var html = Html.fromHtml(it,Html.FROM_HTML_MODE_LEGACY).toString()
                    html = html.pregReplace("\\\\u003C","<")
                    html = html.pregReplace("\\\\","")
                    val ans:ArrayList<String> = arrayListOf()
                    if( html.pregMatche( "<!-- NicoFlickRankingTag=\"(.*?)\" num=\"(\\d+)\" -->", matche = ans ) ){
                        val tags = ans[1]
                        val num = ans[2]
                        var retTag = tags

                        retTag = retTag.pregReplace("\\s*/(and|AND)/\\s*","/and/")
                        retTag = retTag.pregReplace("\\s+"," or ")

                        val condition = SelectConditions(_tags =  retTag, _sortItem = "")
                        val musics = musicDatas._getSelectMusics(selectCondition =  condition, isMe = false)
                        if( musics.size > 0 ){
                            val musicsStr = musics.map { String(it.sqlID) }.joinToString(separator = ",")
                            ServerDataHandler().postTagsToMusics(tags = "${num}:${tags}", musicsStr = musicsStr){}
                        }
                    }
                }

                webView.evaluateJavascript("document.getElementById('${USERDATA.UserIDxxx}').style.color = '#F00';",null)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        println("onPause")
        super.onPause()
    }

}