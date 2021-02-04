package com.mizushiki.nicoflick_a


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_selector_menu_table_for_sort.*

class Activity_SelectorMenuTableForSort : AppCompatActivity() {

    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    val texts = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector_menu_table_for_sort)

        editText_sort.setText( USERDATA.SelectedMusicCondition.sortItem )

        setSupportActionBar(toolbar2)
        getSupportActionBar()!!.setTitle("sort")
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        texts.addAll( USERDATA.SelectedMusicCondition.SortItem )
        // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, texts)
        listView.setAdapter(arrayAdapter)

        // 項目をタップしたときの処理
        listView.setOnItemClickListener {parent, view, position, id ->
            val sp = editText_sort.text.split(" ")
            if( sp.contains(texts[position]) ) {
                return@setOnItemClickListener
            }
            editText_sort.setText(texts[position])
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun Button_Delete(view: View) {
        editText_sort.setText("")
    }

    override fun onPause() {
        println(editText_sort.text.toString())
        USERDATA.SelectedMusicCondition.sortItem = editText_sort.text.toString()
        super.onPause()
    }

}