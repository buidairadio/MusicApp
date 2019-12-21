package com.uet.teamduan.musicplayer.screens.adapters

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Playlist
import com.uet.teamduan.musicplayer.utils.Utils
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity


class PlaylistRecyclerViewAdapter (
    var listener:PlaylistListener,
    var playlistList:ArrayList<Playlist>
    ,var context: Context

): RecyclerView.Adapter<PlaylistRecyclerViewAdapter.PlaylistViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
            LayoutInflater.from(p0.context).inflate(
                R.layout.playlist_row,
                p0, false
            )
        )
    }
    override fun getItemCount(): Int {
        return playlistList.size
    }
    override fun onBindViewHolder(p0: PlaylistViewHolder, p1: Int) {
        val playlist = playlistList[p1]

        p0.tv_title.text = playlist.title
        p0.tv_sub.text = playlist.songList.size.toString()+" song(s)"


        if(HomeScreenActivity.darkModeEnabled){
            p0.cvTitle.setCardBackgroundColor(ResourcesCompat.getColor(context.resources,R.color.card_color_dark,null))
            Glide.with(context).load(R.drawable.ic_more_white).into(p0.iv_option)
        }else {
            p0.cvTitle.setCardBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.card_color, null))
            Glide.with(context).load(R.drawable.ic_more).into(p0.iv_option)
        }

        p0.cv_card.setOnClickListener {
            listener.onItemClick(p1, null,"")
        }
        p0.cv_option.setOnClickListener{
            listener.onPlaylistOptionClick(p1,it)
        }
        if(playlist.songList.size>0&&playlist.songList[0].thumb!=null)
            p0.iv_poster.setImageBitmap(playlist.songList[0].thumb)
        else
        Glide.with(context).load(
            if(HomeScreenActivity.darkModeEnabled){R.drawable.ic_playlist_dark}else{R.drawable.ic_playlist}).into(p0.iv_poster)

        Utils.animateRecyclerView(context,p0.itemView)
    }

    class PlaylistViewHolder(view: View): RecyclerView.ViewHolder(view){
        var tv_title: TextView
        var tv_sub: TextView
        var cv_option: CardView
        var iv_option: ImageView
        var iv_poster:ImageView
        var cv_card: CardView
        var cvTitle:CardView
        init{
            tv_title = view.findViewById(R.id.tv_playlist_title)
            tv_sub= view.findViewById(R.id.tv_playlist_sub)
            cv_option = view.findViewById(R.id.cv_playlist_options)
            iv_option = view.findViewById(R.id.iv_playlist_options)
            iv_poster = view.findViewById(R.id.iv_playlist_poster)
            cv_card = view.findViewById(R.id.cv_playlist_card)
            cvTitle = view.findViewById(R.id.cv_playlist_row_title)
        }
    }

    interface PlaylistListener{
        fun onItemClick(item:Int,songList:ArrayList<Song>?, listType:String)
        fun onPlaylistOptionClick(item:Int,view:View)
    }
}