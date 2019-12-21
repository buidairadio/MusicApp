package com.uet.teamduan.musicplayer.screens.adapters

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Category
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity
import com.uet.teamduan.musicplayer.utils.ListTypes
import com.uet.teamduan.musicplayer.utils.Utils

class AlbumRecyclerViewAdapter (
    var listener:AlbumListener,
    var albumList:ArrayList<Category>,
    var listType:String,
    var context: Context
): RecyclerView.Adapter<PlaylistRecyclerViewAdapter.PlaylistViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PlaylistRecyclerViewAdapter.PlaylistViewHolder {
        return PlaylistRecyclerViewAdapter.PlaylistViewHolder(
            LayoutInflater.from(p0.context).inflate(
                R.layout.playlist_row,
                p0, false
            )
        )
    }
    override fun getItemCount(): Int {
        return albumList.size
    }
    override fun onBindViewHolder(p0: PlaylistRecyclerViewAdapter.PlaylistViewHolder, p1: Int) {
        val album = albumList[p1]

        p0.tv_title.text = album.title
        p0.tv_sub.text = album.songList.size.toString() + " song(s)"


        if (HomeScreenActivity.darkModeEnabled) {
            p0.cvTitle.setCardBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.card_color_dark,
                    null
                )
            )
            Glide.with(context).load(R.drawable.ic_more_white).into(p0.iv_option)
        } else {
            p0.cvTitle.setCardBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.card_color,
                    null
                )
            )
            Glide.with(context).load(R.drawable.ic_more).into(p0.iv_option)
        }


        if (album.art != null)
            Glide.with(context).load(album.art).into(p0.iv_poster)
        else {
            Glide.with(context).load(

                    if (listType == ListTypes.LIST_TYPE_ARTIST_SONGS)
                        if (HomeScreenActivity.darkModeEnabled) {
                            R.drawable.ic_artist_dark
                        } else {
                            R.drawable.ic_artist
                        }
                    else
                        if (HomeScreenActivity.darkModeEnabled) {
                            R.drawable.ic_albums_dark
                        } else {
                            R.drawable.ic_albums
                        }

            ).into(p0.iv_poster)
        }
        p0.cv_card.setOnClickListener {
            listener.onItemClick(p1,albumList,listType)
        }
        p0.cv_option.setOnClickListener{
            listener.onPlaylistOptionClick(p1,it,listType)
        }
        Utils.animateRecyclerView(context,p0.itemView)
    }


    interface AlbumListener{
        fun onItemClick(item:Int, list:ArrayList<Category>, listType:String)
        fun onPlaylistOptionClick(item:Int,view:View,listType:String)
    }
}