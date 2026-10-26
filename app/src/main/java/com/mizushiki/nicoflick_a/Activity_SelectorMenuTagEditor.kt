package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivitySelectorMenuTagEditorBinding


import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import com.eclipsesource.json.Json

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Activity_SelectorMenuTagEditor : SafeAreaActivity() {

    private lateinit var binding: ActivitySelectorMenuTagEditorBinding


    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    val texts = arrayListOf<String>()

    var maeText = ""
    var smNum = ""
    var sqlID = -1

    var postText = ""
    var segueing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectorMenuTagEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        maeText = getIntent().getStringExtra("tagstr") ?: ""
        smNum = getIntent().getStringExtra("smNum") ?: ""
        sqlID = getIntent().getIntExtra("sqlID",-1)

        binding.editTextHensyu.setText( maeText )

        setSupportActionBar(binding.toolbar2)
        getSupportActionBar()!!.setTitle("tag (music)")
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        /*
        // インテントのインスタンス作成
        val data = Intent()
        // インテントに値をセット
        data.putExtra("modori", modori);
        // 結果を設定
        setResult(RESULT_OK, data)
        finish()
         */
        //遷移間の返り値が何故か上手く拾えないのでグローバルに頼る
        GLOBAL.retString = postText
        println("ONPAUSE!!")
        super.onPause()
    }

    fun Button_postTags(view: View) {
        if(segueing) {
            return
        }
        if( sqlID == -1 ){
            return
        }
        postText = binding.editTextHensyu.text.toString().trim().replace("\n", " ").pregReplace("\\s+", " ")
        if (postText == "" || postText == maeText) {
            return
        }

        val id = sqlID
        segueing = true
        binding.progressCircular.isVisible = true

        ServerDataHandler().postMusicTagUpdate(id, postText, USERDATA.UserID) {
            if (!it) {
                binding.progressCircular.isVisible = false
                postText = ""
                segueing = false
                AlertDialog.Builder(this)
                    .setMessage("登録できませんでした。")
                    .setPositiveButton("OK", null)
                    .show()
                return@postMusicTagUpdate
            }
            ServerDataHandler().DownloadMusicData {
                if (it != null) {
                    println("music-load error")
                }
                binding.progressCircular.isVisible = false
                segueing = false
                //onPauseでGLOBAL.retString = postText
                finish()
            }
        }
    }
    fun Button_GetNicoTag(view: View) {

        binding.progressCircular.isVisible = true

        GetNicoTags(smNum) {
            binding.progressCircular.isVisible = false
            if(it=="error"){
                //ダメだった
                return@GetNicoTags
            }
            binding.editTextNicoTag.setText(Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY))
            binding.editTextNicoTag.isVisible = true
        }
    }

    fun GetNicoTags(smNum: String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.NicoApiURL_GetThumbInfo + smNum
        println(url)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            println(it)
//            println(it.pregMatche("<nicovideo_thumb_response status=\"ok\">"))
            if( it.pregMatche("<nicovideo_thumb_response status=\"ok\">") ){
//                println(it.pregMatche("<tags.*?>(.*?)</tags>"))
//                println(it.pregMatche_firstString("<tags.*?>(.*?)</tags>"))
                callback( it.pregMatche_firstString("<tags.*?>(.*?)</tags>").pregReplace("<tag.*?>", "").pregReplace("</tag>", " ").pregReplace("\n|\r\n|\r", "").pregReplace(" +", " ").trim() )
//                callback( it.pregMatche_firstString("<tags.*?>(.*?)</tags>").pregReplace("</?tag.*?>","").pregReplace("\n"," ").pregReplace(" +"," ").trim() )
                return@let
            }
        }
    }
}