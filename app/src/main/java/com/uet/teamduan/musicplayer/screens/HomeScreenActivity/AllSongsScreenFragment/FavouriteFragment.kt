package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide

import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity


class FavouriteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return initView(inflater.inflate(R.layout.fragment_favourite, container, false))
    }

    private fun initView(view :View):View{
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_favourite_fragment)
        val playAllButton = view.findViewById<CardView>(R.id.cv_play_all_favourite)
        Glide.with(activity as HomeScreenActivity).load(if(HomeScreenActivity.darkModeEnabled){R.drawable.ic_play_all_dark}else{R.drawable.ic_play_all}).into(
            view.findViewById<ImageView>(R.id.iv_play_all_favourite))
        playAllButton.setOnClickListener{
            (activity as HomeScreenActivity).onPlayAllButtonClickedInFavouriteSongFragment()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = (activity as HomeScreenActivity).favouriteAdapter
        return view
    }

}
