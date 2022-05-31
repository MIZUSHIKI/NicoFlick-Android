package com.mizushiki.nicoflick_a


import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_selector.*
import kotlinx.android.synthetic.main.activity_selector_menu_table_for_sort.*
import java.util.*


class Activity_SelectorMenuTableForSort : AppCompatActivity() {

    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    val texts = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector_menu_table_for_sort)

        editText_sort.setText(USERDATA.SelectedMusicCondition.sortItem)

        setSupportActionBar(toolbar2)
        getSupportActionBar()!!.setTitle("sort")
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        texts.addAll(USERDATA.SelectedMusicCondition.SortItem)
        // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, texts)
        listView.setAdapter(arrayAdapter)

        // 項目をタップしたときの処理
        listView.setOnItemClickListener { parent, view, position, id ->
            if( editText_sort.text.contains(texts[position]) ){
                return@setOnItemClickListener
            }
            val hosi = editText_sort.text.toString().pregMatche_firstString("( [★☆]{10}[■□])$")
            editText_sort.setText(texts[position] + hosi)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                SESystemAudio.openSubSePlay()
                finish()
            }
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

    fun Button_StarFilter(view: View) {
        val sortStars = SelectConditions().getSortStars(editText_sort.text.toString())

        val items = arrayOf("★1", "★2", "★3", "★4", "★5", "★6", "★7", "★8", "★9", "★10", "FULL")
        val checkedItems = sortStars.mapIndexed { index, star -> if(star) index else (index - 100) }.filter { it >= 0 }.toMutableList()
        AlertDialog.Builder(this)
            .setTitle("表示する難易度")
            .setMultiChoiceItems(items, sortStars.toBooleanArray(),
                OnMultiChoiceClickListener { dialog, which, isChecked ->
                    if (isChecked) checkedItems.add(which) else checkedItems.remove(
                        (which as Int)!!
                    )
                })
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                var stars = mutableListOf(false,false,false,false,false,false,false,false,false,false,false)
                for (i in checkedItems) {
                    // item_i checked
                    stars[i] = true
                }
                var sortitem = editText_sort.text.toString().pregReplace(" [★☆]{10}[■□]$","")
                if( stars.contains(false) ){
                    sortitem += " "
                    stars.forEachIndexed { index, star ->
                        if(index<10){
                            sortitem += if(star) "★" else "☆"
                        }else {
                            sortitem += if(star) "■" else "□"
                        }
                    }
                }
                editText_sort.setText(sortitem)
            })
            .show()

    }

}