package com.uet.teamduan.musicplayer.screens.HomeScreenActivity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.models.provider.LocalSongProvider
import com.uet.teamduan.musicplayer.screens.adapters.PlaylistSongRecyclerViewAdapter
import com.uet.teamduan.musicplayer.utils.ListTypes
import com.uet.teamduan.musicplayer.utils.Utils
import com.uet.teamduan.musicplayer.utils.scaleAndCropCenter

private const val TAG = "SHOW_SONG_LIST_FRAG"
class ShowSongListFragment: Fragment() {

    var listType:String? = null
    var playlistIndex:Int? = null
    var songList:ArrayList<Song>? = null
    var adapter:PlaylistSongRecyclerViewAdapter?=null

    var menuIconView:RelativeLayout? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return initView(inflater.inflate(R.layout.fragment_song_list_display, container, false))
    }

    override fun onStart() {
        val ivPoster = view!!.findViewById<ImageView>(R.id.iv_song_list_display_poster)


        if(songList!![0].thumb!=null){
            val thumb = LocalSongProvider.getThumbail(activity as HomeScreenActivity, songList!![0].albumId)
            val w = (activity as HomeScreenActivity).window.decorView.width
//            ivPoster.setImageBitmap()
            Glide.with(activity as HomeScreenActivity).load(thumb.scaleAndCropCenter(w, Utils.DPToPX(250,activity as HomeScreenActivity))).into(ivPoster)
        }else {

            if(HomeScreenActivity.darkModeEnabled){
                if(listType == ListTypes.LIST_TYPE_ALBUM_SONGS)
                    Glide.with(activity as HomeScreenActivity).load(R.drawable.ic_albums_dark).into(ivPoster)
                else if(listType == ListTypes.LIST_TYPE_ARTIST_SONGS)
                    Glide.with(activity as HomeScreenActivity).load(R.drawable.ic_artist_dark).into(ivPoster)
                else
                    Glide.with(activity as HomeScreenActivity).load(R.drawable.ic_playlist_dark).into(ivPoster)

            }else{
                if(listType == ListTypes.LIST_TYPE_ALBUM_SONGS)
                    Glide.with(activity as HomeScreenActivity).load(R.drawable.ic_albums).into(ivPoster)
                else if(listType == ListTypes.LIST_TYPE_ARTIST_SONGS)
                    Glide.with(activity as HomeScreenActivity).load(R.drawable.ic_artist).into(ivPoster)
            }
        }



        Log.d(TAG,"fragment done")
        super.onStart()
    }


    private fun initView(view : View): View {

        songList = arguments?.getParcelableArrayList("songList")
        playlistIndex = arguments?.getInt("playlistIndex")
        listType = arguments?.getString("listType")



        adapter = PlaylistSongRecyclerViewAdapter((activity as HomeScreenActivity).playlistSongItemListener
            ,songList!!,playlistIndex!!, listType!!,(activity as HomeScreenActivity))

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_song_list)
        view.findViewById<CardView>(R.id.cv_song_list_display_play_all).setOnClickListener(clickListener)
        menuIconView = view.findViewById(R.id.rl_show_more_options_for_menu_item)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        setHasOptionsMenu(true)

        return view
    }
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater): Unit {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.song_list_display_menu, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                (activity as HomeScreenActivity).onBackPressed()
                return true
            }
            R.id.song_list_display_more->{
                (activity as HomeScreenActivity).albumItemListener.onPlaylistOptionClick(playlistIndex!!,menuIconView!!,listType!!)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private val clickListener = View.OnClickListener { p0 ->
        when(p0?.id){
            R.id.cv_song_list_display_play_all->{
                (activity as HomeScreenActivity).onPlayAll(playlistIndex!!,listType!!)
            }
//            R.id.cv_song_list_display_options->{
//                (activity as HomeScreenActivity).albumItemListener.onPlaylistOptionClick(playlistIndex!!,p0,listType!!)
//            }
        }
    }
}