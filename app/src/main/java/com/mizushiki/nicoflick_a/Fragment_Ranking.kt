package com.mizushiki.nicoflick_a

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_selector.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.fragment_ranking.Button_Back
import kotlinx.android.synthetic.main.fragment_ranking.TextView_Star

class RankingFragment : Fragment() {

    var myIndex:Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onStart() {
        super.onStart()
        Button_Back.setOnClickListener{ activity?.finish() }

        text_Title.setText(GLOBAL.SelectMUSIC!!.title)
        TextView_Star.setText(GLOBAL.SelectLEVEL!!.getLevelAsString())

        progress_circular_r.isVisible = true
        ServerDataHandler().DownloadUserName {
            if (it != null) { /*なんらかのエラー*/ }
            ServerDataHandler().GetScoreData(GLOBAL.SelectLEVEL!!.sqlID) {
                if( it != null ){
                    listView.adapter = RankingAdapter(GLOBAL.APPLICATIONCONTEXT, it)
                    progress_circular_r.isVisible = false
                    val myID = USERDATA.UserID
                    println("myID="+myID)
                    for( index in 0 until it.count() ){
                        val jsonObject = it.get(index).asObject()
                        val id = jsonObject.get("userID").asString()
                        println("id="+id)
                        if(id == myID){
                            myIndex = index
                            listView.setSelection(index)
                            break
                        }
                    }
                }
            }
        }

        button_Top.setOnClickListener{
            listView.setSelection(0)
        }
        button_Bottom.setOnClickListener {
            listView.setSelection(listView.count - 1)
        }
        button_My.setOnClickListener {
            myIndex?.let{
                listView.setSelection(it)
            }
        }
    }
}
private class RankingAdapter(val context: Context,
                      val sortedList: JsonArray) : BaseAdapter() {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return sortedList.size()
    }

    override fun getItem(position: Int): JsonObject {
        return sortedList.get(position).asObject()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = layoutInflater.inflate(R.layout.item_ranking_list, parent, false) //表示するレイアウト取得
        val ranktext = view.findViewById<TextView>(R.id.rank)
        ranktext.text = (position+1).toString()

        val jsonObject =  sortedList.get(position).asObject()
        val id = jsonObject.get("userID").asString()
        if( id == USERDATA.UserID ){
            view.findViewById<TextView>(R.id.name).setTextColor(Color.RED)
            view.findViewById<TextView>(R.id.name).text =  USERDATA.UserName
        }else {
            view.findViewById<TextView>(R.id.name).text =  UserNameDataLists.getUserName(id)
        }
        view.findViewById<TextView>(R.id.score).text = jsonObject.get("score").asString()
        return view
    }
}
