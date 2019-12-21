package com.uet.teamduan.musicplayer.screens.adapters

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity
import com.uet.teamduan.musicplayer.services.MusicPlayerService
import com.uet.teamduan.musicplayer.utils.ListTypes


//https://www.androidhive.info/2016/01/android-working-with-recycler-view/

private const val TAG = "SONG_RV_ADAPTER"
class SongRecyclerViewAdapter(
        var listener:StandardSongViewHolder.ItemClickListener<Pair<Int, Song>>?,
        var songList:ArrayList<Song>,
        var moreOptionListener: StandardSongViewHolder.ItemMoreOptionClickListener,
        var context:Context
): RecyclerView.Adapter<StandardSongViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): StandardSongViewHolder {
        return StandardSongViewHolder(
            LayoutInflater.from(p0.context).inflate(
                R.layout.standard_row,
                p0,
                false
            ),moreOptionListener, ListTypes.LIST_TYPE_ALL_SONGS,context
        )
    }
    override fun getItemCount(): Int {
        return songList.size
    }
    override fun onBindViewHolder(p0: StandardSongViewHolder, p1: Int) {
        var song = songList[p1]
        p0.bind(song,-1,p1)


        Log.d(TAG,"Song "+song.title+" isPlaying "+song.isPlaying.toString())
        if(song.isPlaying){

            p0.ivPlaying?.visibility = View.VISIBLE



//            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//
//                if (MusicPlayerService.musicPlayerGlobalState) {
//                    p0.ivPlaying?.visibility = View.VISIBLE
//                }else
//                    p0.ivPlaying?.visibility = View.GONE

//            }else {
                if (MusicPlayerService.musicPlayerGlobalState) {
                    Glide.with(context)
                        .asGif()
                        .load(R.drawable.ic_playing_gif_trans)
                        .into(p0.ivPlaying!!)
                } else {
                    Glide.with(context)
                        .load(R.drawable.ic_playing)
                        .into(p0.ivPlaying!!)
                }
//            }

            p0.title.setTextColor(ResourcesCompat.getColor(context.resources,R.color.textColorOrange,null))
            p0.sub.setTextColor(ResourcesCompat.getColor(context.resources,R.color.textColorOrange,null))

        }else{
            p0.ivPlaying?.visibility = View.GONE
//            p0.ivPlaying2?.visibility = View.GONE
            if(HomeScreenActivity.darkModeEnabled) {
                p0.title.setTextColor(ResourcesCompat.getColor(context.resources, R.color.colorBackgroundLight, null))
                p0.sub.setTextColor(ResourcesCompat.getColor(context.resources, R.color.colorBackgroundLight, null))
            }else{
                p0.title.setTextColor(ResourcesCompat.getColor(context.resources, R.color.textColorDark, null))
                p0.sub.setTextColor(ResourcesCompat.getColor(context.resources, R.color.textColorDark, null))
            }
        }



        p0.itemView.setOnClickListener {
            if(listener!=null)
                listener!!.onItemClick(Pair(p1,song))
        }
//        if(song.isPlaying){
//            p0.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.colorTransparentLight))
//        }else{
//            p0.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.colorAbsoluteTransparency))
//        }
    }
}