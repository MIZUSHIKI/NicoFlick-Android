package com.mizushiki.nicoflick_a

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_selector_menu.*


class Activity_SelectorMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector_menu)

        textView_tagWords.setText(USERDATA.SelectedMusicCondition.tags)
        textView_sortWords.setText(USERDATA.SelectedMusicCondition.sortItem)

        Button_goMovieSite.isVisible = (GLOBAL.SelectMUSIC != null)
    }

    fun Button_HowTo(view: View) {
        val strList = arrayOf("NicoFlickデータベース","NicoFlick紹介 動画")

        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
            .setTitle("ブラウザで開く")
            .setItems(strList, { dialog, which ->
                when(which){
                    0 -> {
                        val url = "http://timetag.main.jp/nicoflick/index.php"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(Intent.createChooser(intent, "Browse with"))
                    }
                    1 -> {
                        val url = "https://www.nicovideo.jp/watch/sm33685683"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(Intent.createChooser(intent, "Browse with"))
                    }
                }
            })
            .setPositiveButton("キャンセル", null)
            .show()
    }
    fun Button_Back(view: View) {
        setResult( 10, null )
        finish()
    }
    fun TextView_tagOnClick(view: View) {
        val intent: Intent = Intent(applicationContext, Activity_SelectorMenuTableForTag::class.java)
        intent.putExtra("fromSelector", false)
        startActivityForResult(intent, 1001)
    }
    fun TextVIew_sortOnClick(view: View) {
        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
            .setTitle("Sort")
            .setSingleChoiceItems(USERDATA.SelectedMusicCondition.SortItem, USERDATA.SelectedMusicCondition.sortItemNum, { dialog, which ->
                USERDATA.SelectedMusicCondition.sortItemNum = which
                textView_sortWords.setText(USERDATA.SelectedMusicCondition.sortItem)
            })
            .setPositiveButton("OK",null)
            .show()
    }

    fun Button_GoMovieSite(view: View) {
        val url = GLOBAL.SelectMUSIC!!.movieURL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(Intent.createChooser(intent, "Browse with"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // startActivityForResult()の際に指定した識別コードとの比較
        if( requestCode == 1001 ){
            textView_tagWords.setText(USERDATA.SelectedMusicCondition.tags)
        }
    }
}
