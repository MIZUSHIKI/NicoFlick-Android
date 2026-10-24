package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.FragmentCommentBinding


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
import androidx.core.view.doOnPreDraw
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject

import java.text.SimpleDateFormat
import java.util.*

class CommentFragment : Fragment() {

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        loading = false
    }


    var commentDatas:CommentDataLists = CommentDataLists

    var loading:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         // Inflate the layout for this fragment
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val host = activity ?: return
        if (host.isFinishing || host.isDestroyed) return
        val selectedMusic = GLOBAL.SelectMUSIC
        val selectedLevel = GLOBAL.SelectLEVEL
        if (selectedMusic == null || selectedLevel == null) {
            host.returnToStartAfterStateLoss()
            return
        }
        val binding = this.binding
        binding.ButtonBack.setOnClickListener{
            SESystemAudio.canselSePlay()
            activity?.finish()
        }

        binding.textTitleC.setText(selectedMusic.title)
        binding.TextViewStar.setText(selectedLevel.getLevelAsString())

        if( GLOBAL.ResultFirst == false ){
            binding.buttonCommentEdit.isEnabled = false
            binding.buttonCommentEdit.setTextColor(Color.DKGRAY)
            binding.buttonCommentEdit.setBackgroundColor(Color.LTGRAY)
            binding.editTextComment.isEnabled = false
            binding.editTextComment.setTextColor(Color.DKGRAY)
            binding.editTextComment.setBackgroundColor(Color.LTGRAY)
            binding.editTextComment.setText("ゲームクリア後リザルト画面からコメント出来ます。")
        }else {
            binding.buttonCommentEdit.isEnabled = true
            binding.buttonCommentEdit.setTextColor(Color.WHITE)
            binding.buttonCommentEdit.setBackgroundColor(Color.rgb(0x00,0x99,0xCC))

            binding.buttonCommentEdit.setOnClickListener{
                if(binding.editTextComment.text.length in 1..65){
                    // println("投稿")
                    GLOBAL.ResultFirst = false
                    binding.buttonCommentEdit.isEnabled = false
                    binding.buttonCommentEdit.setTextColor(Color.DKGRAY)
                    binding.buttonCommentEdit.setBackgroundColor(Color.LTGRAY)
                    binding.editTextComment.isEnabled = false
                    binding.editTextComment.setTextColor(Color.DKGRAY)
                    binding.editTextComment.setBackgroundColor(Color.LTGRAY)
                    binding.progressCircularC.isVisible = true
                    loading = true

                    ServerDataHandler().postComment(binding.editTextComment.text.toString().trim(),selectedLevel.sqlID,USERDATA.UserID){

                        if (_binding !== binding || host.isFinishing || host.isDestroyed) return@postComment
                        if (it) {
                            // 投稿から一覧への反映まで、読み込み表示を継続する。
                            ServerDataHandler().DownloadCommentData(selectedLevel.sqlID) { error ->
                                if (_binding !== binding || host.isFinishing || host.isDestroyed) return@DownloadCommentData
                                if (error != null) {
                                    binding.progressCircularC.isVisible = false
                                    loading = false
                                    AlertDialog.Builder(host)
                                        .setMessage("コメントは投稿されましたが、一覧を更新できませんでした。画面を開き直してください。")
                                        .setPositiveButton("OK", null)
                                        .show()
                                    return@DownloadCommentData
                                }
                                val comments = commentDatas.getSortedComments(selectedLevel.sqlID)
                                binding.listViewC.adapter = CommentAdapter(GLOBAL.APPLICATIONCONTEXT, comments, selectedLevel)
                                binding.progressCircularC.isVisible = false
                                // 更新後のリストが描画された次のタイミングで完了を知らせる。
                                binding.listViewC.doOnPreDraw {
                                    binding.listViewC.post {
                                        if (_binding !== binding || host.isFinishing || host.isDestroyed) return@post
                                        loading = false
                                        AlertDialog.Builder(host)
                                            .setMessage("コメントを投稿しました。")
                                            .setPositiveButton("OK", null)
                                            .show()
                                    }
                                }
                            }
                        } else {
                            binding.progressCircularC.isVisible = false
                            loading = false
                            AlertDialog.Builder(host)
                                .setMessage("コメントの投稿に失敗しました")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                }
            }
            binding.editTextComment.addTextChangedListener(object:TextWatcher{
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.textViewCommentEditCharCount.setText("${s!!.length} /64文字")
                    if(s!!.length > 64){
                        binding.buttonCommentEdit.isEnabled = false
                        binding.buttonCommentEdit.setTextColor(Color.DKGRAY)
                        binding.buttonCommentEdit.setBackgroundColor(Color.LTGRAY)
                    }else {
                        binding.buttonCommentEdit.isEnabled = true
                        binding.buttonCommentEdit.setTextColor(Color.WHITE)
                        binding.buttonCommentEdit.setBackgroundColor(Color.rgb(0x00,0x99,0xCC))
                    }
                }

            })
        }

        binding.progressCircularC.isVisible = true
        loading = true
        ServerDataHandler().DownloadCommentData(selectedLevel.sqlID) {
            if (_binding !== binding || host.isFinishing || host.isDestroyed) return@DownloadCommentData
            val itto = commentDatas.getSortedComments(selectedLevel.sqlID)
            // println("itto=$itto")
            binding.listViewC.adapter = CommentAdapter(GLOBAL.APPLICATIONCONTEXT, itto, selectedLevel)
            binding.progressCircularC.isVisible = false
            loading = false
        }
    }

}

private class CommentAdapter(val context: Context,
                             val sortedList: List<commentData>,
                             val selectedLevel: levelData
) : BaseAdapter() {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return sortedList.count() + ( if(selectedLevel.description!="") 1 else 0 )
    }

    override fun getItem(position: Int): commentData {
        return sortedList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = layoutInflater.inflate(R.layout.item_comment_list, parent, false) //表示するレイアウト取得

        var position = position
        if(selectedLevel.description != ""){
            if(position == 0){
                view.setBackgroundColor(Color.argb(255*50/100,255*60/100,255*90/100,255*30/100))
                val textView_name = view.findViewById<TextView>(R.id.name)
                textView_name.setTextColor(Color.RED)
                textView_name.text = selectedLevel.creator
                view.findViewById<TextView>(R.id.comment).text = selectedLevel.description
                view.findViewById<TextView>(R.id.postedDay).text = "投稿者コメント"
                return view
            }
            position -= 1
        }
        val id = sortedList[position].userID
        if( id == USERDATA.UserID ){
            view.findViewById<TextView>(R.id.name).text = if(USERDATA.UserName != "") USERDATA.UserName else "NO_NAME"
        }else {
            view.findViewById<TextView>(R.id.name).text =  UserNameDataLists.getUserName(id)
        }
        view.findViewById<TextView>(R.id.comment).text = sortedList[position].comment
        val a = sortedList[position].sqlUpdateTime
        val date = Date(a*1000L)
        val f = SimpleDateFormat("yyyy.MM.dd")
        view.findViewById<TextView>(R.id.postedDay).text = f.format(date)
        return view
    }
}

