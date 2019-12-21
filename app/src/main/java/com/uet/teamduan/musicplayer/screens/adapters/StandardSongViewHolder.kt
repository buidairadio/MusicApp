package com.uet.teamduan.musicplayer.screens.adapters

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity

private const val TAG="STANDARD_VIEW_HOLDER"
class StandardSongViewHolder(
    view: View,
    var moreOptionListener: ItemMoreOptionClickListener,
    var listType:String,var context: Context
):RecyclerView.ViewHolder(view){
    var title: TextView
    var sub: TextView
    var thumbail: ImageView
    var moreOption:CardView
    var ivMoreOption:ImageView
    var cvMoreOption:CardView
    var ivPlaying:ImageView? = null
//    var ivPlaying2:ImageView? = null
    var llParent:LinearLayout
    init{
        title = view.findViewById(R.id.tv_standard_row_title)
        sub= view.findViewById(R.id.tv_standard_row_sub_text)
        thumbail = view.findViewById(R.id.iv_standard_row_icon)
        moreOption = view.findViewById(R.id.cv_standard_row_options)
        ivMoreOption = view.findViewById(R.id.iv_standard_row_options)
        cvMoreOption = view.findViewById(R.id.cv_standard_row_options)
        llParent = view.findViewById(R.id.ll_standard_row_parent)
        ivPlaying = view.findViewById(R.id.iv_standard_row_playing)


//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ivPlaying2 = addDancingMusic(context)
//            ivPlaying2?.visibility = View.GONE
//        }
    }

    fun bind(song: Song, playlistIndex:Int,songIndex:Int){
        title.text = song.title
        sub.text = song.artist
        if(song.thumb!=null)
            Glide.with(context).load(song.thumb).into(thumbail)
        else
            Glide.with(context).load(R.drawable.ic_songs).into(thumbail)


        Glide.with(context).load(if(HomeScreenActivity.darkModeEnabled){R.drawable.ic_more_white}else{R.drawable.ic_more}).into(ivMoreOption)


        moreOption.setOnClickListener(object:View.OnClickListener{
            override fun onClick(v: View?) {
                moreOptionListener.onItemMoreOptionClick(song,itemView,adapterPosition,playlistIndex,listType)
            }
                //if the service has not been started yet, we push song to the queue on main activity
                //and that is when the player lick the playMusicPlayer button for the first time. we will send that queue to the
                //service. and when the user open the queue. they will see their songs over there
                //in case the service has already started. we simply send the song to the queue inside that service
        })
    }
//    fun addDancingMusic(context: Context):ImageView{
//        val v = GifImageView(context)
//        val p = LinearLayout.LayoutParams(Utils.DPToPX(20, context), Utils.DPToPX(20, context))
//
//        v.setImageResource(R.drawable.ic_playing_gif_trans)
//        v.isClickable = false
//        v.layoutParams = p
//        p.setMargins(Utils.DPToPX(15, context), 0, 0, 0)
//
//        llParent.removeView(cvMoreOption)
//        llParent.addView(v)
//        llParent.addView(cvMoreOption)
//        //ivPlaying = v
//        return v
//    }
    interface ItemMoreOptionClickListener{
        fun onItemMoreOptionClick(song:Song,view:View, songIndex:Int,playlistIndex:Int,listType:String)
    }

    interface ItemClickListener<T>{
        fun onItemClick(item:T)
    }
}