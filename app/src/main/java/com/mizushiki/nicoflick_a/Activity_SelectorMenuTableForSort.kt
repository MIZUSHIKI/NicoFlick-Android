package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivitySelectorMenuTableForSortBinding


import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import java.util.*


class Activity_SelectorMenuTableForSort : AppCompatActivity() {

    private lateinit var binding: ActivitySelectorMenuTableForSortBinding


    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    val texts = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectorMenuTableForSortBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTextSort.setText(USERDATA.SelectedMusicCondition.sortItem)

        setSupportActionBar(binding.toolbar2)
        getSupportActionBar()!!.setTitle("sort")
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        texts.addAll(USERDATA.SelectedMusicCondition.SortItem)
        // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
        val icons = listOf(R.drawable.music_new, R.drawable.music_old, R.drawable.flick_new, R.drawable.flick_old,
            R.drawable.plays_new, R.drawable.plays_old, R.drawable.favorites_many, R.drawable.favorites_few,
            R.drawable.video_id_large, R.drawable.video_id_small, R.drawable.highscore_recent, R.drawable.comment_recent,
            R.drawable.plays_recent, R.drawable.plays_user)
        val iconSize = (32 * resources.displayMetrics.density).toInt()
        val arrayAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, texts) {
            override fun getView(position:Int, convertView:View?, parent:ViewGroup):View {
                val row = super.getView(position, convertView, parent) as TextView
                val icon = androidx.core.content.ContextCompat.getDrawable(context, icons[position])
                icon?.setBounds(0, 0, iconSize, iconSize)
                row.setCompoundDrawables(icon, null, null, null)
                row.compoundDrawablePadding = (12 * resources.displayMetrics.density).toInt()
                return row
            }
        }
        binding.listView.setAdapter(arrayAdapter)

        // 項目をタップしたときの処理
        binding.listView.setOnItemClickListener { parent, view, position, id ->
            if( binding.editTextSort.text.contains(texts[position]) ){
                return@setOnItemClickListener
            }
            val hosi = binding.editTextSort.text.toString().pregMatche_firstString("( [★☆]{10}[■□])$")
            binding.editTextSort.setText(texts[position] + hosi)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                SESystemAudio.openSubSePlay()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun Button_Delete(view: View) {
        binding.editTextSort.setText("")
    }

    override fun onPause() {
        // println(binding.editTextSort.text.toString())
        USERDATA.SelectedMusicCondition.sortItem = binding.editTextSort.text.toString()
        super.onPause()
    }

    fun Button_StarFilter(view: View) {
        val sortStars = SelectConditions().getSortStars(binding.editTextSort.text.toString())

        val items = arrayOf("★1", "★2", "★3", "★4", "★5", "★6", "★7", "★8", "★9", "★10", "FULL")
        val checkedItems = sortStars.mapIndexed { index, star -> if(star) index else (index - 100) }.filter { it >= 0 }.toMutableList()
        AlertDialog.Builder(this)
            .setTitle("表示する難易度")
            .setMultiChoiceItems(items, sortStars.toBooleanArray(),
                OnMultiChoiceClickListener { dialog, which, isChecked ->
                    if (isChecked) checkedItems.add(which) else checkedItems.remove(
                        (which as Int)!!
                    )
                })
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                var stars = mutableListOf(false,false,false,false,false,false,false,false,false,false,false)
                for (i in checkedItems) {
                    // item_i checked
                    stars[i] = true
                }
                var sortitem = binding.editTextSort.text.toString().pregReplace(" [★☆]{10}[■□]$","")
                if( stars.contains(false) ){
                    sortitem += " "
                    stars.forEachIndexed { index, star ->
                        if(index<10){
                            sortitem += if(star) "★" else "☆"
                        }else {
                            sortitem += if(star) "■" else "□"
                        }
                    }
                }
                binding.editTextSort.setText(sortitem)
            })
            .show()

    }

}