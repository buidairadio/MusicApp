package com.uet.teamduan.musicplayer.screens.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Song

private const val TAG = "PL_SONG_RC_ADAPTER"
class PlaylistSongRecyclerViewAdapter(
    var listener:StandardSongViewHolder.ItemClickListener<Pair<String,Pair<Int, Int>>>,
    var songList:ArrayList<Song>,
    var playlistIndex:Int,
    var listType:String,
    var moreOptionListener: StandardSongViewHolder.ItemMoreOptionClickListener
): RecyclerView.Adapter<StandardSongViewHolder>() {

    init{
        Log.d(TAG," init PlaylistSongRecyclerViewAdapter")
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): StandardSongViewHolder {
        Log.d(TAG,"creating view holder 1")
        return StandardSongViewHolder(
            LayoutInflater.from(p0.context).inflate(
                R.layout.standard_row,
                p0,
                false
            ),moreOptionListener,listType,p0.context
        )
    }
    override fun getItemCount(): Int {
        Log.d(TAG,"getItemCount")
        return songList.size
    }
    override fun onBindViewHolder(p0: StandardSongViewHolder, p1: Int) {
        Log.d(TAG,"onBindViewHolder")
        var song = songList[p1]
        p0.bind(song,playlistIndex,p1)
        p0.ivPlaying?.visibility = View.GONE
//        p0.ivPlaying2?.visibility = View.GONE

        p0.itemView.setOnClickListener {
            listener.onItemClick(Pair(listType,Pair(p0.adapterPosition,playlistIndex)))
        }
        Log.d(TAG,"binding view holder ")
    }
}