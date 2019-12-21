package com.uet.teamduan.musicplayer.screens.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity

class SongSelectRecyclerViewAdapter(
    var listener:SongSelectListener,
    var favouriteList:ArrayList<Song>
    ,var context: Context

): RecyclerView.Adapter<SongSelectRecyclerViewAdapter.SongSelectViewHolder>() {
    var selectAll:Boolean = false
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SongSelectViewHolder {
        return SongSelectViewHolder(
            LayoutInflater.from(p0.context).inflate(
                R.layout.song_select_row,
                p0, false
            ))
    }
    override fun getItemCount(): Int {
        return favouriteList.size
    }
    override fun onBindViewHolder(p0: SongSelectViewHolder, p1: Int) {
        val song = favouriteList[p1]

        p0.title.text = song.title
        p0.sub.text = song.artist

        Glide.with(context).load(if(HomeScreenActivity.darkModeEnabled){R.drawable.ic_more_white}else{R.drawable.ic_more}).into(p0.iv_more)



        if(song.thumb!=null)
            Glide.with(context).load(song.thumb).into(p0.thumbail)
        else
            Glide.with(context).load(R.drawable.ic_songs).into(p0.thumbail)

        if(song.selected){
            p0.background.setBackgroundColor(ContextCompat.getColor(context,R.color.colorTransparentLight))
            Glide.with(context).load(R.drawable.ic_tick).into(p0.selectState)
        }else{
            p0.background.setBackgroundColor(ContextCompat.getColor(context,R.color.colorAbsoluteTransparency))

            if(HomeScreenActivity.darkModeEnabled)
                Glide.with(context).load(R.drawable.ic_tick_no_dark).into(p0.selectState)
            else
                Glide.with(context).load(R.drawable.ic_tick_no).into(p0.selectState)
        }

        p0.cv_select.setOnClickListener{
            listener.onItemClick(p0,p1,song)
            notifyItemChanged(p1)
        }

        p0.itemView.setOnClickListener {
            listener.onItemClick(p0,p1,song)
            notifyItemChanged(p1)
        }
    }

    class SongSelectViewHolder(
        view: View
    ): RecyclerView.ViewHolder(view){
        var title: TextView
        var sub: TextView
        var thumbail: ImageView
        var selectState: ImageView
        var background:CardView
        var cv_select: CardView
        var cv_more:CardView
        var iv_more:ImageView
        init{
            title = view.findViewById(R.id.tv_song_select_title)
            sub= view.findViewById(R.id.tv_song_select_sub_text)
            thumbail = view.findViewById(R.id.iv_song_select_icon)
            selectState = view.findViewById(R.id.iv_song_select_state)
            background = view.findViewById(R.id.cv_song_select_backgound)
            cv_select = view.findViewById(R.id.cv_song_select_state)
            cv_more = view.findViewById(R.id.cv_song_select_options)
            iv_more = view.findViewById(R.id.iv_song_select_options)

            cv_more.visibility =View.GONE
        }
    }

    interface SongSelectListener{
        fun onItemClick(holder:SongSelectViewHolder,index:Int, song:Song)
    }
}