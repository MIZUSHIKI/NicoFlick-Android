package com.mizushiki.nicoflick_a

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_ranking_comment.*
import kotlinx.android.synthetic.main.activity_result.*

class Activity_RankingComment : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {

    val rankingFragment = RankingFragment()
    val commentFragment = CommentFragment()
    var added:Boolean = false //・・・
    var maeItemId = -1

    val selectMusic = GLOBAL.SelectMUSIC!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking_comment)

        btm_navigation.setOnNavigationItemSelectedListener(this)

        // FrameLayout のインスタンスを取得
        val slashShadeLayout: FrameLayout = findViewById(R.id.RankingCommentView_SlashShade_layout)
        val slashShadeView = SlashShadeView(this,slashColor=Color.argb(255,204,255,102) )
        slashShadeLayout.addView(slashShadeView)
        PicassoLoadImage_NicoThumb(RankingCommentView_imageAlpha, selectMusic.thumbnailURL)

        if( savedInstanceState == null ){
            supportFragmentManager.beginTransaction()
                .add(R.id.frameLayout, rankingFragment)
                .commit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        println("rankingFragment.loading= ${rankingFragment.loading}")
        println("commentFragment.loading= ${commentFragment.loading}")
        if( rankingFragment.loading == true || commentFragment.loading == true ){
            return false
        }
        if( maeItemId != item.itemId ){
            maeItemId = item.itemId
            SESystemAudio.openSubSePlay()
        }
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
                if(!commentFragment.isAdded && !added){
                    added = true
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