package com.mizushiki.nicoflick_a


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_selector_menu_tag_editor.*

class Activity_SelectorMenuTagEditor : AppCompatActivity() {

    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    val texts = arrayListOf<String>()

    var maeText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector_menu_tag_editor)

        maeText = getIntent().getStringExtra("tagstr") ?: ""
        editText_hensyu.setText( maeText )

        setSupportActionBar(toolbar2)
        getSupportActionBar()!!.setTitle("tag")
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
        println(editText_hensyu.text.toString())
        var modori =  editText_hensyu.text.toString()
        if( modori == maeText ){
            modori = ""
        }
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
        GLOBAL.retString = modori
        super.onPause()
    }

}