package com.mizushiki.nicoflick_a


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import kotlinx.android.synthetic.main.activity_how_to_game.*
import kotlinx.android.synthetic.main.fragment_how_to_game.*
import kotlinx.android.synthetic.main.fragment_ranking.*

class Fragment_HowToGame : Fragment() {

    companion object {
        fun newInstance(Rdrawable: Int): Fragment_HowToGame {
            return Fragment_HowToGame().apply {
                arguments = Bundle().apply {
                    putInt("Rdrawable", Rdrawable)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_how_to_game, container, false)
    }

    override fun onStart() {
        super.onStart()
        button_HowToGame_OK.setOnClickListener{ activity?.finish() }

        val Rdrawable = arguments!!.getInt("Rdrawable")
        if( Rdrawable == 0 ){
            imageView_HowToGame.isVisible = false
            return
        }
        imageView_HowToGame.setImageResource(Rdrawable)
    }
}

class HowToGameAdapter(fm: FragmentManager, private val fragmentList: List<Fragment>) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    // 表示するフラグメントを制御する
    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    // viewPagerにセットするコンテンツ(フラグメントリスト)のサイズ
    override fun getCount(): Int {
        return fragmentList.size
    }
}