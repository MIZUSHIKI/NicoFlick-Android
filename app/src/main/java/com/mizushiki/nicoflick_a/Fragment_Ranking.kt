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
import androidx.core.view.isInvisible
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
    var userNameDatas:UserNameDataLists = UserNameDataLists
    var scoreDatas:ScoreDataLists = ScoreDataLists
    var rankingData:List<scoreData> = listOf()

    var loading:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onStart() {
        super.onStart()
        Button_Back.setOnClickListener{ activity?.finish() }

        text_Title.setText(GLOBAL.SelectMUSIC!!.title)
        TextView_Star.setText(GLOBAL.SelectLEVEL!!.getLevelAsString())
        if( userNameDatas.usernameJsonNumCount >= 0 ){
            textView_usernameNum.text = "ユーザーネームデータ分割${userNameDatas.usernameJsonNumCount}まで取得済み"
        }

        progress_circular_r.isVisible = true
        loading = true
        ServerDataHandler().DownloadUserNameData {
            if (it != null) { /*なんらかのエラー*/ }
            ServerDataHandler().DownloadScoreData(GLOBAL.SelectLEVEL!!.sqlID){
                progress_circular_r.isVisible = false
                loading = false

                if(userNameDatas.usernameJsonNumCount == -1){
                    textView_usernameNum.isVisible = false
                }
                textView_usernameNum.text = "ユーザーネームデータ分割${userNameDatas.usernameJsonNumCount}まで取得済み"

                val itto = scoreDatas.getSortedScores(GLOBAL.SelectLEVEL!!.sqlID)
                listView.adapter = RankingAdapter(GLOBAL.APPLICATIONCONTEXT, itto)

                val myID = USERDATA.UserID
                println("myID="+myID)

                for( (index,scoredata) in itto.withIndex()){
                    val id = scoredata.userID
                    if(id == myID){
                        myIndex = index
                        var ind = index -3
                        if(ind < 0){ ind = 0 }
                        listView.setSelection(ind)
                        break
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
                var ind = it -3
                if(ind < 0){ ind = 0 }
                listView.setSelection(ind)
            }
        }
    }
}
private class RankingAdapter(val context: Context,
                      val sortedList: List<scoreData>) : BaseAdapter() {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return sortedList.count()
    }

    override fun getItem(position: Int): scoreData {
        return sortedList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = layoutInflater.inflate(R.layout.item_ranking_list, parent, false) //表示するレイアウト取得
        val ranktext = view.findViewById<TextView>(R.id.rank)
        ranktext.text = (position+1).toString()

        val id = sortedList[position].userID
        if( id == USERDATA.UserID ){
            view.findViewById<TextView>(R.id.name).setTextColor(Color.RED)
            view.findViewById<TextView>(R.id.name).text = if(USERDATA.UserName != "") USERDATA.UserName else "NO_NAME"
        }else {
            view.findViewById<TextView>(R.id.name).text =  UserNameDataLists.getUserName(id)
        }
        view.findViewById<TextView>(R.id.score).text = sortedList[position].score.toString()
        return view
    }
}
