package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivityRankingCommentBinding

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class Activity_RankingComment : SafeAreaActivity(),BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityRankingCommentBinding


    private lateinit var rankingFragment: RankingFragment
    private lateinit var commentFragment: CommentFragment
    var added:Boolean = false //・・・
    var maeItemId = -1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectMusic = GLOBAL.SelectMUSIC ?: return returnToStartAfterStateLoss()
        if (GLOBAL.SelectLEVEL == null) return returnToStartAfterStateLoss()

        // FragmentManagerが復元したインスタンスを表示切替にも使用する。
        rankingFragment = supportFragmentManager.findFragmentByTag("ranking") as? RankingFragment
            ?: supportFragmentManager.fragments.filterIsInstance<RankingFragment>().firstOrNull()
            ?: RankingFragment()
        commentFragment = supportFragmentManager.findFragmentByTag("comment") as? CommentFragment
            ?: supportFragmentManager.fragments.filterIsInstance<CommentFragment>().firstOrNull()
            ?: CommentFragment()
        added = commentFragment.isAdded
        binding.btmNavigation.setOnNavigationItemSelectedListener(this)

        // FrameLayout のインスタンスを取得
        val slashShadeLayout: FrameLayout = findViewById(R.id.RankingCommentView_SlashShade_layout)
        val slashShadeView = SlashShadeView(this,slashColor=Color.argb(255,204,255,102) )
        slashShadeLayout.addView(slashShadeView)
        PicassoLoadImage_NicoThumb(binding.RankingCommentViewImageAlpha, selectMusic.thumbnailURL)

        if( savedInstanceState == null ){
            supportFragmentManager.beginTransaction()
                .add(R.id.frameLayout, rankingFragment, "ranking")
                .commit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (isFinishing || !::rankingFragment.isInitialized || !::commentFragment.isInitialized) return false
        // println("rankingFragment.loading= ${rankingFragment.loading}")
        // println("commentFragment.loading= ${commentFragment.loading}")
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
                if (commentFragment.isAdded) sfmbt.hide(commentFragment)
                sfmbt.commit()
                return true
            }
            R.id.item2 -> {
                val sfmbt = supportFragmentManager.beginTransaction()
                if(!commentFragment.isAdded && !added){
                    added = true
                    sfmbt.add(R.id.frameLayout, commentFragment, "comment")
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