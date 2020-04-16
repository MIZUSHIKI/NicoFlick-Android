package com.mizushiki.nicoflick_a

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
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
        Button_postReport.isVisible = (GLOBAL.SelectMUSIC != null)
        if(USERDATA.RepotedMusicID.contains("${GLOBAL.SelectMUSIC?.sqlID}")){
            Button_postReport.isEnabled = false
            Button_postReport.setBackgroundColor(Color.LTGRAY)
        }
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

    fun Button_postReport(view: View) {
        val editText = EditText(this);
        editText.setHint("具体的な違反内容を記入");
        AlertDialog.Builder(this)
            .setTitle("違反報告")
            .setMessage("この動画の利用が著作者の権利を侵害していることを報告します。\n\n・無断でアップロードされた楽曲\n・二次三次創作がある程度許容されうるジャンル（環境）ではない 等\n")
            .setView(editText)
            .setPositiveButton("OK", DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
                // TextViewにセットしてあげる
                val musicID = GLOBAL.SelectMUSIC!!.sqlID
                ServerDataHandler().postReport(musicID,editText.text.toString(),USERDATA.UserID){
                    if(it){
                        val rmid = USERDATA.RepotedMusicID
                        rmid.add("$musicID")
                        USERDATA.RepotedMusicID = rmid
                        Button_postReport.isEnabled = false
                        Button_postReport.setBackgroundColor(Color.LTGRAY)
                    }
                }
            })
            .setNegativeButton("キャンセル", null)
            .show()
    }
}
