package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.FragmentHowToGameBinding


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


class Fragment_HowToGame : Fragment() {

    private var _binding: FragmentHowToGameBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


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
        _binding = FragmentHowToGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.buttonHowToGameOK.setOnClickListener{ activity?.finish() }

        val Rdrawable = arguments!!.getInt("Rdrawable")
        if( Rdrawable == 0 ){
            binding.imageViewHowToGame.isVisible = false
            return
        }
        binding.buttonHowToGameOK.isVisible = false
        binding.imageViewHowToGame.setImageResource(Rdrawable)
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