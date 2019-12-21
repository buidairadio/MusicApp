package com.uet.teamduan.musicplayer.screens

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity.Companion.darkModeEnabled
import com.uet.teamduan.musicplayer.screens.adapters.SongSelectRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_song_select.*

private const val TAG = "SONG_SELECT_ACTIVITY"
class SongSelectActivity : AppCompatActivity()
    , SongSelectRecyclerViewAdapter.SongSelectListener{

    val selectedSongs = ArrayList<Song>()
    val selectedMap = LinkedHashMap<Int, Int>()
    var songList:ArrayList<Song>? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        if(darkModeEnabled)
            setTheme(R.style.AppThemeDark)
        else
            setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_select)


        songList = intent.getParcelableArrayListExtra("songList")

        rv_song_select.layoutManager = LinearLayoutManager(this)
        rv_song_select.itemAnimator = DefaultItemAnimator()
        rv_song_select.adapter = SongSelectRecyclerViewAdapter(this,songList!!, this)

        cv_song_select_ok.setOnClickListener(listner)
        cv_song_select_cancel.setOnClickListener(listner)
        cv_song_select_all.setOnClickListener(listner)


        if(darkModeEnabled)
            Glide.with(this).load(R.drawable.ic_tick_no_dark).into(iv_song_select_all)
        else
            Glide.with(this).load(R.drawable.ic_tick_no).into(iv_song_select_all)

        changeBackground()

        selectedMap.clear()
    }

    override fun onItemClick(holder: SongSelectRecyclerViewAdapter.SongSelectViewHolder,index:Int, song:Song) {
        Log.d(TAG,"Song selected "+song.title+" index "+index)

        song.selected = !song.selected

        //press one time we add it to the list
        // press the second time we remove it from the list.
        if(song.selected)
            selectedSongs.add(song)
        else {
            selectedSongs.remove(song)


            if(darkModeEnabled)
                Glide.with(this).load(R.drawable.ic_tick_no_dark).into(iv_song_select_all)
            else
                Glide.with(this).load(R.drawable.ic_tick_no).into(iv_song_select_all)
        }
    }



    val listner = object: View.OnClickListener{
        override fun onClick(v: View?) {
            val a = (rv_song_select.adapter as SongSelectRecyclerViewAdapter)
            when(v!!.id){
                R.id.cv_song_select_cancel->{
                    setResult(Activity.RESULT_CANCELED)
                    for(song in selectedSongs)
                        song.selected = false
                    finish()
                }
                R.id.cv_song_select_ok->{
                    val intent = Intent()
                    intent.putParcelableArrayListExtra("selectedSongs", if(a.selectAll) {songList}else{selectedSongs})
                    setResult(Activity.RESULT_OK,intent)
                    for(song in selectedSongs)
                        song.selected = false
                    finish()
                }
                R.id.cv_song_select_all->{
                    if(songList?.size != selectedSongs.size){
                        for(song in songList!!) {
                            if(!song.selected){
                                song.selected = true
                                selectedSongs.add(song)
                            }
                        }
                        Glide.with(this@SongSelectActivity).load(R.drawable.ic_tick).into(iv_song_select_all)
                    }else{
                        for(song in songList!!) {
                            song.selected = false
                        }
                        selectedSongs.clear()

                        if(darkModeEnabled)
                            Glide.with(this@SongSelectActivity).load(R.drawable.ic_tick_no_dark).into(iv_song_select_all)
                        else
                            Glide.with(this@SongSelectActivity).load(R.drawable.ic_tick_no).into(iv_song_select_all)
                    }
                    rv_song_select.adapter?.notifyDataSetChanged()
                }
            }
        }
    }
    private fun changeBackground(){
        if(darkModeEnabled) {
            val sdk = Build.VERSION.SDK_INT
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                ll_song_select.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundDark))
            } else {
                ll_song_select.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundDark))
            }
            changeToDarkIcons()
        }else{
            val sdk = Build.VERSION.SDK_INT
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                ll_song_select.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundLight))
            } else {
                ll_song_select.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundLight))
            }
            changeToLightIcons()
        }
    }
    private fun changeToLightIcons(){
    }
    private fun changeToDarkIcons(){
    }
}


