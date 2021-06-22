package com.mizushiki.nicoflick_a

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.progress_circular
import kotlinx.android.synthetic.main.activity_selector_menu_table_for_tag.*
import okhttp3.internal.format


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
            texts.addAll(GLOBAL.SelectMUSIC!!.tag)
        }else {
            texts.addAll(musicDatas.taglist.toList().sortedBy { -it.second }.map { it.first })
            Button_edimaru.isVisible = false
        }
        // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, texts)
        listView.setAdapter(arrayAdapter)

        // 項目をタップしたときの処理
        listView.setOnItemClickListener { parent, view, position, id ->
            val sp = editText_tags.text.split(" ")
            if( sp.contains(texts[position]) ) {
                return@setOnItemClickListener
            }
            var jikanSitei = ""
            if( sp.any { it.startsWith("@t:") } ) {
                sp.firstOrNull { it.startsWith("@t:") }?.let {
                    jikanSitei = " " + it
                }
                //時間指定が機能するように一度削除して最後にもっていく
                editText_tags.setText( editText_tags.text.toString().pregReplace("\\s?@t:(\\d+:\\d+)?-?(\\d+:\\d+)?", "") )
            }
            if( !editText_tags.text.isEmpty() && !editText_tags.text.endsWith("-") ){
                editText_tags.text.append(" ")
            }
            editText_tags.text.append(texts[position] + jikanSitei)
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
        intent.putExtra("sqlID", GLOBAL.SelectMUSIC!!.sqlID )
        intent.putExtra("smNum", GLOBAL.SelectMUSIC!!.movieURL.pregMatche_firstString("watch/(\\w*\\d+)") )
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
                val postText = modoriStr?.trim()?.replace("\n", " ")?.pregReplace("\\s+", " ") ?: ""
                if (postText == "") {
                    return
                }
                //リストを更新
                texts = arrayListOf()
                texts.addAll(postText.split(" "))
                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, texts)
                listView.setAdapter(arrayAdapter)
            }
        }
    }

    fun Button_jikanSitei(view: View) {
        val layout = LinearLayout(this)
        layout.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        val picker1_m = NumberPicker(this)
        picker1_m.scrollBy(0, 99)
        picker1_m.minValue = 0
        picker1_m.maxValue = 99
        val label1 = TextView(this)
        label1.setText(" : ")
        val picker1_s = NumberPicker(this)
        picker1_s.scrollBy(0, 59)
        picker1_s.minValue = 0
        picker1_s.maxValue = 59
        val label2 = TextView(this)
        label2.setText("　～　")
        val picker2_m = NumberPicker(this)
        picker2_m.scrollBy(0, 99)
        picker2_m.minValue = 0
        picker2_m.maxValue = 99
        picker2_m.value = 3
        val label3 = TextView(this)
        label3.setText(" : ")
        val picker2_s = NumberPicker(this)
        picker2_s.scrollBy(0, 59)
        picker2_s.minValue = 0
        picker2_s.maxValue = 59
        layout.addView(picker1_m)
        layout.addView(label1)
        layout.addView(picker1_s)
        layout.addView(label2)
        layout.addView(picker2_m)
        layout.addView(label3)
        layout.addView(picker2_s)

        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
            .setTitle("時間指定")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", { dialog, which ->
                // Yesが押された時の挙動
                println("${picker1_m.value}:${picker1_s.value} ~ ${picker2_m.value}:${picker2_s.value}")
                var m1 = picker1_m.value
                var s1 = picker1_s.value
                var m2 = picker2_m.value
                var s2 = picker2_s.value
                if( m1*60+s1 > m2*60+s2 ){
                    m2 = picker1_m.value
                    s2 = picker1_s.value
                    m1 = picker2_m.value
                    s1 = picker2_s.value
                }
                if( m1==0 && s1==0 && m2==99 && s2==59 ){
                    return@setPositiveButton
                }

                val sp = editText_tags.text.split(" ")
                if( sp.any { it.startsWith("@t:") } ) {
                    //含まれていたら一度削除
                    editText_tags.setText( editText_tags.text.toString().pregReplace("\\s?@t:(\\d+:\\d+)?-?(\\d+:\\d+)?", "") )
                }

                if( editText_tags.text.toString() != "" ) {
                    editText_tags.text.append(" ")
                }
                editText_tags.text.append("@t:")
                if( m1 != 0 || s1 != 0 ){
                    editText_tags.text.append( String.format("%d:%02d",m1,s1) )
                }
                editText_tags.text.append("-")
                if( m2 != 99 || s2 != 59 ){
                    editText_tags.text.append( String.format("%d:%02d",m2,s2) )
                }
            })
            .show()
    }
}
