package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivityHowToGameBinding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager


class Activity_HowToGame : AppCompatActivity() {

    private lateinit var binding: ActivityHowToGameBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHowToGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /// フラグメントのリストを作成
        val fragmentList = arrayListOf<Fragment>(
            Fragment_HowToGame.newInstance(R.drawable.howto1a),
            Fragment_HowToGame.newInstance(R.drawable.howto2a),
            Fragment_HowToGame.newInstance(0)
        )

        /// adapterのインスタンス生成
        val adapter = HowToGameAdapter(supportFragmentManager, fragmentList)
        /// adapterをセット
        binding.viewPagerHowToGame.adapter = adapter

        // 最後のページに来たらnextボタンを消す
        binding.viewPagerHowToGame.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if( position >= fragmentList.size-1){
                    binding.buttonNextPage.isVisible = false
                }
            }
        })
        //最下のボタン非表示
        val view = window.decorView
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
    }

    fun Button_NextPage(view: View) {
        // ページを1つ進める
        binding.viewPagerHowToGame.currentItem += 1
    }
}
