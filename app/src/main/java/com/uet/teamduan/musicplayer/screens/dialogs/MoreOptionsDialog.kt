package com.uet.teamduan.musicplayer.screens.dialogs

import android.content.Context
import android.os.Build
import android.support.v7.widget.CardView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.bumptech.glide.Glide
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.utils.ListTypes
import com.uet.teamduan.musicplayer.utils.Utils

private const val TAG = "MORE_OPTIONS_DIALOG"
object MoreOptionsDialog {
    fun showPopupWindowQueueItem(context:Context, view:View, index:Int,callback:MoreOptionActionCallback, isBottom:Boolean){

        val inflater  = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.more_option_popup_window,null, false)
        val pw = PopupWindow(v,Utils.DPToPX(100,context), Utils.DPToPX(40,context),true)

        if(isBottom)
            pw.showAsDropDown(view,-Utils.DPToPX(25,context),0,Gravity.BOTTOM or Gravity.END)
        else
            pw.showAsDropDown(view,-Utils.DPToPX(25,context),0,Gravity.TOP or Gravity.END)

        val delete = v.findViewById<CardView>(R.id.cv_more_option_add_to_next)
        (v.findViewById<TextView>(R.id.tv_more_option_add_to_next)).text = "Delete"
        Glide.with(context).load(R.drawable.ic_delete).into(v.findViewById<ImageView>(R.id.iv_more_option_add_to_next))

        delete.setOnClickListener{
            callback.onDelete(index)
            pw.dismiss()
        }
    }
    fun showPopupWindow1(context:Context, view:View, song:Song,songIndex:Int,playlistIndex:Int, callback:MoreOptionActionCallback, isBottom:Boolean,listType: String){

        val inflater  = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.more_option_popup_window,null, false)

        val pw = PopupWindow(v,Utils.DPToPX(140,context), Utils.DPToPX(
            120
                    +if(listType == ListTypes.LIST_TYPE_PLAYLIST_SONGS){40}else{0}
                    +if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){40}else{0}
            ,context),true)
//        pw.showAtLocation(view,Gravity.BOTTOM or Gravity.END,view.x.toInt(),view.y.toInt())

        if(isBottom)
            pw.showAsDropDown(view,-Utils.DPToPX(25,context),0,Gravity.BOTTOM or Gravity.END)
        else
            pw.showAsDropDown(view,-Utils.DPToPX(25,context),0,Gravity.TOP or Gravity.END)

        v.findViewById<TextView>(R.id.tv_more_option_like).text = if(song.liked){ "Unlike" }else{"Like"}
        Glide.with(context).load(if(song.liked){ R.drawable.ic_like_click}else{R.drawable.ic_like}).into(v.findViewById(R.id.iv_more_option_like))


        val buttonClickListener = object:View.OnClickListener{
            override fun onClick(p0: View?) {
                when(p0!!.id){
                    R.id.cv_more_option_add_to_next->{
                        callback.onAddToNext(song)
                    }
                    R.id.cv_more_option_push_to_queue->{
                        callback.onPushToQueue(song)
                    }
                    R.id.cv_more_option_share->{
                        //callback.onShare(song)
                    }
                    R.id.cv_more_option_like->{
                        callback.onLikeFromOptionsDialog(song)
                    }
                    R.id.cv_more_option_delete->{
                        callback.onDeleteSongInPlaylist(songIndex,playlistIndex)
                    }
                    R.id.cv_more_option_cancel->{

                    }
                }
                pw.dismiss()
            }
        }

        v.findViewById<CardView>(R.id.cv_more_option_add_to_next).setOnClickListener(buttonClickListener)
        v.findViewById<CardView>(R.id.cv_more_option_push_to_queue).setOnClickListener(buttonClickListener)
        v.findViewById<CardView>(R.id.cv_more_option_share).setOnClickListener(buttonClickListener)
        v.findViewById<CardView>(R.id.cv_more_option_like).setOnClickListener(buttonClickListener)



        val delete = v.findViewById<CardView>(R.id.cv_more_option_delete)
        if(listType == ListTypes.LIST_TYPE_PLAYLIST_SONGS) {
            delete.visibility = View.VISIBLE
            delete.setOnClickListener (buttonClickListener)
        }

        val cancel = v.findViewById<CardView>(R.id.cv_more_option_cancel)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            cancel.visibility = View.VISIBLE
            cancel.setOnClickListener(buttonClickListener)
        }
    }


    fun showPopupWindow2(context:Context, view:View, index:Int,type:String, callback:MoreOptionActionCallback2, isBottom:Boolean,isOpenFromFragment:Boolean ){
        val inflater  = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.more_option_popup_window_2,null, false)
        val height = if(type == ListTypes.LIST_TYPE_PLAYLIST_SONGS){200}else{120}

        val pw = PopupWindow(v,Utils.DPToPX(140,context), Utils.DPToPX(height
                + if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){40}else{0}
            ,context),true)
//        pw.showAtLocation(view,Gravity.BOTTOM or Gravity.END,view.x.toInt(),view.y.toInt())

        if (isBottom)
            pw.showAsDropDown(view, -Utils.DPToPX(20,context), 0, Gravity.BOTTOM or Gravity.END)
        else
            pw.showAsDropDown(view, -Utils.DPToPX(20,context), 0, Gravity.TOP or Gravity.END)

        val addToNext = v.findViewById<CardView>(R.id.cv_more_option_2_add_to_next)
        val pushToQueue = v.findViewById<CardView>(R.id.cv_more_option_2_push_to_queue)
        val playAll = v.findViewById<CardView>(R.id.cv_more_option_2_play_all)

        playAll.setOnClickListener {
            callback.onPlayAll(index,type)
            pw.dismiss()
        }
        addToNext.setOnClickListener{
            Log.d(TAG,"Button 1 on dialog clicked")
            callback.onAddToNext2(index,type)
            pw.dismiss()
        }
        pushToQueue.setOnClickListener{
            Log.d(TAG,"Button 1 on dialog clicked")
            callback.onPushToQueue2(index,type)
            pw.dismiss()
        }

        val delete = v.findViewById<CardView>(R.id.cv_more_option_2_delete)
        val rename = v.findViewById<CardView>(R.id.cv_more_option_2_rename)
        if(type == ListTypes.LIST_TYPE_PLAYLIST_SONGS){
            delete.setOnClickListener{
                callback.onDelete2(index,type,isOpenFromFragment)
                pw.dismiss()
            }
            rename.setOnClickListener {
                callback.onRename2(index, type)
                pw.dismiss()
            }
        }else{
            delete.visibility = View.GONE
            rename.visibility = View.GONE
        }

        val cancel = v.findViewById<CardView>(R.id.cv_more_option_2_cancel)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            cancel.visibility = View.VISIBLE
            cancel.setOnClickListener{ pw.dismiss() }

        }
    }

    fun showPopupWindow3(context:Context, view:View, isBottom:Boolean ,callback:MoreOptionActionCallback3,song:Song){
        val inflater  = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.more_option_popup_window_2,null, false)
        val height = 40//if(type == ListTypes.LIST_TYPE_PLAYLIST_SONGS){200}else{120}

        val pw = PopupWindow(v,Utils.DPToPX(140,context), Utils.DPToPX(height
                + if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){40}else{0}
            ,context),true)
//        pw.showAtLocation(view,Gravity.BOTTOM or Gravity.END,view.x.toInt(),view.y.toInt())

        if (isBottom)
            pw.showAsDropDown(view, -Utils.DPToPX(20,context), 0, Gravity.BOTTOM or Gravity.END)
        else
            pw.showAsDropDown(view, -Utils.DPToPX(20,context), 0, Gravity.TOP or Gravity.END)

        val addToNext = v.findViewById<CardView>(R.id.cv_more_option_2_add_to_next)
        val pushToQueue = v.findViewById<CardView>(R.id.cv_more_option_2_push_to_queue)
        val playAll = v.findViewById<CardView>(R.id.cv_more_option_2_play_all)
        val delete = v.findViewById<CardView>(R.id.cv_more_option_2_delete)
        val rename = v.findViewById<CardView>(R.id.cv_more_option_2_rename)
        val cancel = v.findViewById<CardView>(R.id.cv_more_option_2_cancel)
        addToNext.visibility = View.GONE
        pushToQueue.visibility = View.GONE
        playAll.visibility = View.GONE
        rename.visibility = View.GONE
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            cancel.visibility = View.VISIBLE
            cancel.setOnClickListener{ pw.dismiss() }

        }
        delete.setOnClickListener {
            callback.onDeleteOnlineSong(song)
            pw.dismiss()
        }
    }

    interface MoreOptionActionCallback{
        fun onAddToNext(song:Song)
        fun onPushToQueue(song:Song)
        fun onDelete(index:Int)
        fun onDeleteSongInPlaylist(songIndex:Int,playlistIndex:Int)
        fun onLikeFromOptionsDialog(song:Song)
        fun onShare(song:Song)
    }
    interface MoreOptionActionCallback2{
        fun onAddToNext2(index:Int, listType:String)
        fun onPushToQueue2(index:Int, listType:String)
        fun onRename2(index:Int, listType:String)
        fun onDelete2(index:Int, listType:String,isOpenFromFragment:Boolean)
        fun onPlayAll(index:Int, listType:String)
    }
    interface MoreOptionActionCallback3{
        fun onDeleteOnlineSong(song:Song)
    }
}