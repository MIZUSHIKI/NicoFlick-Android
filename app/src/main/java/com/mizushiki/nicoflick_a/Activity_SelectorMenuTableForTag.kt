package com.mizushiki.nicoflick_a

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_selector_menu_table_for_tag.*

class Activity_SelectorMenuTableForTag : AppCompatActivity() {

    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    val texts = arrayListOf<String>()

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
}
