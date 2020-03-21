package com.mizushiki.nicoflick_a


import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.isVisible
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.fragment_comment.Button_Back
import kotlinx.android.synthetic.main.fragment_comment.TextView_Star
import kotlinx.android.synthetic.main.fragment_ranking.*
import java.text.SimpleDateFormat
import java.util.*

class CommentFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onStart() {
        super.onStart()
        Button_Back.setOnClickListener{ activity?.finish() }

        text_Title_c.setText(GLOBAL.SelectMUSIC!!.title)
        TextView_Star.setText(GLOBAL.SelectLEVEL!!.getLevelAsString())

        if( GLOBAL.ResultFirst == false ){
            button_commentEdit.isEnabled = false
            button_commentEdit.setTextColor(Color.DKGRAY)
            button_commentEdit.setBackgroundColor(Color.LTGRAY)
            editText_comment.isEnabled = false
            editText_comment.setTextColor(Color.DKGRAY)
            editText_comment.setBackgroundColor(Color.LTGRAY)
            editText_comment.setText("ゲームクリア後リザルト画面からコメント出来ます。")
        }else {
            button_commentEdit.isEnabled = true
            button_commentEdit.setTextColor(Color.WHITE)
            button_commentEdit.setBackgroundColor(Color.rgb(0x00,0x99,0xCC))

            button_commentEdit.setOnClickListener{
                if(editText_comment.text.length in 1..65){
                    println("投稿")
                    GLOBAL.ResultFirst = false
                    button_commentEdit.isEnabled = false
                    button_commentEdit.setTextColor(Color.DKGRAY)
                    button_commentEdit.setBackgroundColor(Color.LTGRAY)
                    editText_comment.isEnabled = false
                    editText_comment.setTextColor(Color.DKGRAY)
                    editText_comment.setBackgroundColor(Color.LTGRAY)
                    progress_circular_c.isVisible = true

                    ServerDataHandler().postComment(editText_comment.text.toString().trim(),GLOBAL.SelectLEVEL!!.sqlID,USERDATA.UserID){
                        if(it){
                            progress_circular_c.isVisible = false
                            AlertDialog.Builder(activity)
                                .setMessage("投稿しました。")
                                .setPositiveButton("OK", { dialog, which ->
                                    progress_circular_c.isVisible = true
                                    ServerDataHandler().GetCommentData(GLOBAL.SelectLEVEL!!.sqlID) {
                                        if( it != null ){
                                            listView_c.adapter = CommentAdapter(GLOBAL.APPLICATIONCONTEXT, it)
                                            progress_circular_c.isVisible = false
                                        }
                                    }
                                })
                                .show()
                        }
                    }
                }
            }
            editText_comment.addTextChangedListener(object:TextWatcher{
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    textView_commentEditCharCount.setText("${s!!.length} /64文字")
                    if(s!!.length > 64){
                        button_commentEdit.isEnabled = false
                        button_commentEdit.setTextColor(Color.DKGRAY)
                        button_commentEdit.setBackgroundColor(Color.LTGRAY)
                    }else {
                        button_commentEdit.isEnabled = true
                        button_commentEdit.setTextColor(Color.WHITE)
                        button_commentEdit.setBackgroundColor(Color.rgb(0x00,0x99,0xCC))
                    }
                }

            })
        }

        progress_circular_c.isVisible = true
        ServerDataHandler().GetCommentData(GLOBAL.SelectLEVEL!!.sqlID) {
            if( it != null ){
                listView_c.adapter = CommentAdapter(GLOBAL.APPLICATIONCONTEXT, it)
                progress_circular_c.isVisible = false
            }
        }
    }

}

private class CommentAdapter(val context: Context,
                             val sortedList: JsonArray
) : BaseAdapter() {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return sortedList.size() + ( if(GLOBAL.SelectLEVEL!!.description!="") 1 else 0 )
    }

    override fun getItem(position: Int): JsonObject {
        return sortedList.get(position).asObject()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = layoutInflater.inflate(R.layout.item_comment_list, parent, false) //表示するレイアウト取得

        var position = position
        if(GLOBAL.SelectLEVEL!!.description != ""){
            if(position == 0){
                view.setBackgroundColor(Color.GREEN)
                val textView_name = view.findViewById<TextView>(R.id.name)
                textView_name.setTextColor(Color.RED)
                textView_name.text = GLOBAL.SelectLEVEL!!.creator
                view.findViewById<TextView>(R.id.comment).text = GLOBAL.SelectLEVEL!!.description
                view.findViewById<TextView>(R.id.postedDay).text = "投稿者コメント"
                return view
            }
            position -= 1
        }
        val jsonObject =  sortedList.get(position).asObject()
        view.findViewById<TextView>(R.id.name).text =  UserNameDataLists.getUserName(jsonObject.get("userID").asString())
        view.findViewById<TextView>(R.id.comment).text = jsonObject.get("comment").asString()
        val a = jsonObject.get("updateTime").asString().toInt()
        val date = Date(a*1000L)
        val f = SimpleDateFormat("yyyy.MM.dd")
        view.findViewById<TextView>(R.id.postedDay).text = f.format(date)
        return view
    }
}

