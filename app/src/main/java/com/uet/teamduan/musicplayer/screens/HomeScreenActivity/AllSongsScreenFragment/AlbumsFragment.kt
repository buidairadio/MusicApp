package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity


class AlbumsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return initView(inflater.inflate(R.layout.fragment_playlist, container, false))
    }

    fun initView(view:View):View{
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_playlist_fragment)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        val gridLayoutManager= GridLayoutManager(activity,2)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = (activity as HomeScreenActivity).albumAdapter
        return view
    }
}
