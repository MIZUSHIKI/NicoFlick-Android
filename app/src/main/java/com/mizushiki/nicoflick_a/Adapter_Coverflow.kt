package com.mizushiki.nicoflick_a

import android.content.Context
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView


class CoverFlowAdapter(context: Context, musics:ArrayList<musicData>) : BaseAdapter() {
    private var mData: ArrayList<musicData> = ArrayList(0)
    private val mContext: Context
    fun setData(data: ArrayList<musicData>) {
        mData = data
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(pos: Int): Any {
        return mData[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var rowView: View? = convertView
        if (rowView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.item_coverflow, null)
            val viewHolder = ViewHolder()
            viewHolder.imageView = rowView.findViewById(R.id.imageView) as ImageView
            rowView.setTag(viewHolder)
        }

        val holder = rowView!!.getTag() as ViewHolder
        PicassoLoadImage_NicoThumb(holder.imageView, mData[position].thumbnailURL)
        //Picasso.get().load(url).resize(sz.width,sz.height).centerCrop().into(holder.imageView)
        //holder.imageView!!.setImageResource(R.mipmap.ic_launcher)//(mData[position].imageResId)
        return rowView
    }

    internal class ViewHolder {
        //var text: TextView? = null
        var imageView: ImageView? = null
    }

    init {
        mContext = context
        mData = musics
    }
}