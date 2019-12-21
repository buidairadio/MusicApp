package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide

import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity

private const val TAG = "SONG_FRAGMENT"
class SongsFragment : Fragment()
{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initView(inflater.inflate(R.layout.fragment_songs, container, false))
    }


    private fun initView(view :View):View{
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_songs_fragment)
        val playAll = view.findViewById<CardView>(R.id.cv_play_all_song)

        Glide.with(activity as HomeScreenActivity).load(if(HomeScreenActivity.darkModeEnabled){R.drawable.ic_play_all_dark}else{R.drawable.ic_play_all}).into(
            view.findViewById(R.id.iv_play_all_song))

        playAll.setOnClickListener{
            (activity as HomeScreenActivity).playAllSong(0)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = (activity as HomeScreenActivity).adapter

        Log.d(TAG,"Oncreated")
        return view
    }

    override fun onStart() {
        Log.d(TAG,"Onstart")
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG,"Onstop")
    }
}

