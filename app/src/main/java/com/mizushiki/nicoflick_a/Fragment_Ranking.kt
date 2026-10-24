package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.FragmentRankingBinding

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
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnNextLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class RankingFragment : Fragment() {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onDestroyView() {
        super.onDestroyView()
        musicScoreDialog?.dismiss()
        musicScoreDialog = null
        pendingMusicScoreMessage = null
        musicScoreLoading = false
        rankingLoadRequest++
        _binding = null
        loading = false
    }


    var myIndex:Int? = null
    var userNameDatas:UserNameDataLists = UserNameDataLists
    var scoreDatas:ScoreDataLists = ScoreDataLists
    var rankingData:List<scoreData> = listOf()
    var musicScoreMessage = ""
    private var musicScoreLoading = false
    private var rankingLoadRequest = 0
    private var pendingMusicScoreMessage: String? = null
    private var musicScoreDialog: AlertDialog? = null

    private fun showPendingMusicScore() {
        val host = activity ?: return
        if (_binding == null || !isResumed || isHidden || host.isFinishing || host.isDestroyed) return
        if (musicScoreDialog?.isShowing == true) return
        val message = pendingMusicScoreMessage ?: return
        pendingMusicScoreMessage = null
        musicScoreDialog = AlertDialog.Builder(host)
            .setTitle("この楽曲の最大スコア")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        showPendingMusicScore()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            musicScoreDialog?.dismiss()
            musicScoreDialog = null
        } else showPendingMusicScore()
    }

    override fun onStop() {
        musicScoreDialog?.dismiss()
        musicScoreDialog = null
        super.onStop()
    }

    var loading:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
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

        binding.textTitle.setText(selectedMusic.title)
        binding.TextViewStar.setText(selectedLevel.getLevelAsString())
        if( userNameDatas.usernameJsonNumCount >= 0 ){
            binding.textViewUsernameNum.text = "ユーザーネームデータ分割${userNameDatas.usernameJsonNumCount}まで取得済み"
        }

        binding.progressCircularR.isVisible = true
        loading = true
        val loadRequest = ++rankingLoadRequest
        ServerDataHandler().DownloadUserNameData {
            if (_binding !== binding || loadRequest != rankingLoadRequest || host.isFinishing || host.isDestroyed) return@DownloadUserNameData
            if (it != null) { /*なんらかのエラー*/ }
            ServerDataHandler().DownloadScoreData(selectedLevel.sqlID){
                if (_binding !== binding || loadRequest != rankingLoadRequest || host.isFinishing || host.isDestroyed) return@DownloadScoreData
                binding.progressCircularR.isVisible = musicScoreLoading
                loading = false

                if(userNameDatas.usernameJsonNumCount == -1){
                    binding.textViewUsernameNum.isVisible = false
                }
                binding.textViewUsernameNum.text = "ユーザーネームデータ分割${userNameDatas.usernameJsonNumCount}まで取得済み"

                val itto = scoreDatas.getSortedScores(selectedLevel.sqlID)
                binding.listView.adapter = RankingAdapter(GLOBAL.APPLICATIONCONTEXT, itto)

                val myID = USERDATA.UserID
                // println("myID="+myID)

                for( (index,scoredata) in itto.withIndex()){
                    val id = scoredata.userID
                    if(id == myID){
                        myIndex = index
                        var ind = index -3
                        if(ind < 0){ ind = 0 }
                        binding.listView.setSelection(ind)
                        break
                    }
                }
            }

        }

        var scrollRequest = 0
        fun scrollToRankingPosition(position: Int) {
            val listView = binding.listView
            if (listView.count == 0) return
            val targetPosition = position.coerceIn(0, listView.count - 1)
            val currentPosition = listView.firstVisiblePosition
            val request = ++scrollRequest
            val threshold = 10
            if (kotlin.math.abs(targetPosition - currentPosition) > threshold) {
                val nearbyPosition = if (targetPosition > currentPosition) {
                    targetPosition - threshold * 2
                } else {
                    targetPosition + threshold
                }
                // 移動方向に合わせて手前へジャンプし、行配置の確定後にアニメーションする。
                listView.doOnNextLayout {
                    if (_binding === binding && request == scrollRequest) {
                        listView.smoothScrollToPositionFromTop(targetPosition, 0, 300)
                    }
                }
                listView.setSelectionFromTop(nearbyPosition.coerceIn(0, listView.count - 1), 0)
            } else {
                listView.smoothScrollToPositionFromTop(targetPosition, 0, 300)
            }
        }

        binding.buttonTop.setOnClickListener{
            scrollToRankingPosition(0)
        }
        binding.buttonBottom.setOnClickListener {
            scrollToRankingPosition(binding.listView.count - 1)
        }
        binding.buttonMy.setOnClickListener {
            myIndex?.let{
                scrollToRankingPosition((it - 3).coerceAtLeast(0))
            }
        }
        binding.buttonMusicScore.setOnClickListener {
            if (musicScoreLoading || musicScoreDialog?.isShowing == true ||
                pendingMusicScoreMessage != null || host.isFinishing || host.isDestroyed) {
                return@setOnClickListener
            }
            SESystemAudio.openSePlay()
            if (musicScoreMessage.isNotEmpty()) {
                pendingMusicScoreMessage = musicScoreMessage
                showPendingMusicScore()
                return@setOnClickListener
            }
            musicScoreLoading = true
            binding.buttonMusicScore.isEnabled = false
            binding.progressCircularR.isVisible = true
            var completed = false
            ServerDataHandler().GetMusicScoreData(selectedMusic.sqlID, USERDATA.UserID) { response ->
                if (completed || _binding !== binding || host.isFinishing || host.isDestroyed) {
                    return@GetMusicScoreData
                }
                completed = true
                musicScoreLoading = false
                binding.buttonMusicScore.isEnabled = true
                binding.progressCircularR.isVisible = loading
                val records = parseMusicScoreResponse(response)
                // 空応答や通信失敗も記録なしとして記憶し、再タップで再取得しない。
                musicScoreMessage = if (records.isNullOrEmpty()) {
                    "\nあなたの記録はありません"
                } else {
                    val formatter = SimpleDateFormat("yyyy/M/d", Locale.getDefault())
                    records.joinToString(separator = "\n", prefix = "\n") {
                        "${it.label}：${it.score} - ${formatter.format(Date(it.updatedAtMillis))}"
                    }
                }
                pendingMusicScoreMessage = musicScoreMessage
                showPendingMusicScore()
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
