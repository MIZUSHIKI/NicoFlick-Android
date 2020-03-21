package com.mizushiki.nicoflick_a

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_ranking_comment.*
import kotlinx.android.synthetic.main.activity_result.*

class Activity_RankingComment : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {

    val rankingFragment = RankingFragment()
    val commentFragment = CommentFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking_comment)

        btm_navigation.setOnNavigationItemSelectedListener(this)

        if( savedInstanceState == null ){
            supportFragmentManager.beginTransaction()
                .add(R.id.frameLayout, rankingFragment)
                .commit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item1 -> {
                val sfmbt = supportFragmentManager.beginTransaction()
                sfmbt.show(rankingFragment)
                sfmbt.hide(commentFragment)
                sfmbt.commit()
                return true
            }
            R.id.item2 -> {
                val sfmbt = supportFragmentManager.beginTransaction()
                if(!commentFragment.isAdded){
                    sfmbt.add(R.id.frameLayout, commentFragment)
                }
                sfmbt.hide(rankingFragment)
                sfmbt.show(commentFragment)
                sfmbt.commit()
                return true
            }
        }
        return false
    }

}