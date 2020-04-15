package com.mizushiki.nicoflick_a

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_how_to_game.*

class Activity_HowToGame2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_game2)

        /// フラグメントのリストを作成
        val fragmentList = arrayListOf<Fragment>(
            Fragment_HowToGame.newInstance(R.drawable.howto_ime1a),
            Fragment_HowToGame.newInstance(0)
        )

        /// adapterのインスタンス生成
        val adapter = HowToGameAdapter(supportFragmentManager, fragmentList)
        /// adapterをセット
        viewPager_HowToGame.adapter = adapter

        // 最後のページに来たらnextボタンを消す
        viewPager_HowToGame.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if( position >= fragmentList.size-1){
                    button_nextPage.isVisible = false
                }
            }
        })
        //最下のボタン非表示
        val view = window.decorView
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
    }

    fun Button_NextPage(view: View) {
        // ページを1つ進める
        viewPager_HowToGame.currentItem += 1
    }
}
