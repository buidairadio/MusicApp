package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.PlayingQueueFragment

import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.view.PagerAdapter
import android.view.View
import com.uet.teamduan.musicplayer.R


class PosterPagerAdapter(private val mContext: Context) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val view = if(position==0){R.id.cv_player_screen_poster}else{R.id.rl_queue_display}
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(view, collection, false)
        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

}