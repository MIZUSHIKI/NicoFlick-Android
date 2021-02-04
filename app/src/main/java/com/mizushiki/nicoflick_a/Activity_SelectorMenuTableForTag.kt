package com.mizushiki.nicoflick_a

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_selector_menu_table_for_tag.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_main.progress_circular

class Activity_SelectorMenuTableForTag : AppCompatActivity() {

    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    var texts = arrayListOf<String>()

    var segueing = false //遷移中フラグ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector_menu_table_for_tag)

        editText_tags.setText(USERDATA.SelectedMusicCondition.tags)

        setSupportActionBar(toolbar2)
        getSupportActionBar()!!.setTitle("tag")
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        if( getIntent().getBooleanExtra("fromSelector", false) ){
            texts.addAll( GLOBAL.SelectMUSIC!!.tag )
        }else {
            texts.addAll(musicDatas.taglist.toList().sortedBy { -it.second }.map { it.first })
            button8.isVisible = false
        }
        // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, texts)
        listView.setAdapter(arrayAdapter)

        // 項目をタップしたときの処理
        listView.setOnItemClickListener {parent, view, position, id ->
            val sp = editText_tags.text.split(" ")
            if( sp.contains(texts[position]) ) {
                return@setOnItemClickListener
            }
            if( !editText_tags.text.isEmpty() && !editText_tags.text.endsWith("-") ){
                editText_tags.text.append(" ")
            }
            editText_tags.text.append(texts[position])
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun Button_Delete(view: View) {
        editText_tags.setText("")
    }

    override fun onPause() {
        println(editText_tags.text.toString())
        USERDATA.SelectedMusicCondition.tags = editText_tags.text.toString()
        super.onPause()
    }

    fun Button_SyokiGakkyoku(view: View) {
        editText_tags.setText("@初期楽曲")
    }

    fun Button_Hensyu(view: View) {
        val intent: Intent = Intent(applicationContext, Activity_SelectorMenuTagEditor::class.java)
        intent.putExtra("tagstr", GLOBAL.SelectMUSIC!!.tag.joinToString(separator = " "))
        startActivityForResult(intent, 1001)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // startActivityForResult()の際に指定した識別コードとの比較
        when( requestCode ){
            1001 -> {
                //val modoriStr = data?.getStringExtra("modori") ?: ""
                //遷移間の返り値が何故か上手く拾えないのでグローバルに頼る
                val modoriStr = GLOBAL.retString
                GLOBAL.retString = null
                println("modori = $modoriStr")
                val postText = modoriStr?.trim()?.replace("\n"," ")?.pregReplace("\\s+"," ") ?: ""
                if( postText == "" ){
                    return
                }
                //リストを更新
                texts = arrayListOf()
                texts.addAll( postText.split(" ") )
                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, texts)
                listView.setAdapter(arrayAdapter)

                GLOBAL.SelectMUSIC?.sqlID?.let {
                    val id = it
                    segueing = true
                    progress_circular.isVisible = true

                    ServerDataHandler().postMusicTagUpdate(id, postText, USERDATA.UserID){
                        if( !it ){
                            progress_circular.isVisible = false
                            segueing = false
                        }
                        ServerDataHandler().DownloadMusicData {
                            if (it != null) {
                                println("music-load error")
                            }
                            progress_circular.isVisible = false
                            segueing = false
                        }
                    }
                }
            }
        }
    }
}
