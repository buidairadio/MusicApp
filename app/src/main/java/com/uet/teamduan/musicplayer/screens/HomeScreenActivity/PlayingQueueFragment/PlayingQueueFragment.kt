package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.PlayingQueueFragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity
import com.uet.teamduan.musicplayer.utils.Utils
import java.lang.IndexOutOfBoundsException

const val TAG = "PLAYING_QUEUE_FRAGMENT"
class PlayingQueueFragment: Fragment(), HomeScreenActivity.ServiceSongQueueCallback {

    var recyclerView:RecyclerView? = null
    var homeScreenActivity:HomeScreenActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"HELLO PLAYING QUEUE FRAGMENT")
        // Inflate the layout for this fragment
        return initView(inflater.inflate(R.layout.fragment_playing_queue, container, false))
    }


    private fun initView(view : View): View {

        recyclerView = view.findViewById(R.id.rv_playing_queue_fragment)

        recyclerView?.layoutManager = object: LinearLayoutManager(activity){
            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
                try{
                    super.onLayoutChildren(recycler, state)
                }catch (e:IndexOutOfBoundsException){
                    e.printStackTrace()
                }
            }
        }
        homeScreenActivity = (activity as HomeScreenActivity)
        recyclerView?.itemAnimator = DefaultItemAnimator()
        (recyclerView?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            homeScreenActivity!!.aCopyOfCurrentSongIndexToCarryWithinThisClass, Utils.DPToPX(180,activity!!))


        val a = homeScreenActivity!!.queueAdapter
        if(a!=null) {
            recyclerView?.adapter = a
            a.itemTouchHelper.attachToRecyclerView(recyclerView)
        }

        Log.d(TAG,"created QUEUE FRAGMENT")
        return view
    }
    override fun onStart() {
        callback?.fragmentReady()
        Log.d(TAG,"started QUEUE FRAGMENT")
        super.onStart()
    }
    override fun onStop(){
        //homeScreenActivity!!.queueFragmentOpened = false
        Log.d(TAG,"Stopped QUEUE FRAGMENT")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG,"Destroyed QUEUE FRAGMENT ")
        super.onDestroy()
    }
    override fun notifyQueueChanged() {
        recyclerView?.adapter!!.notifyDataSetChanged()
        (recyclerView?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            homeScreenActivity!!.aCopyOfCurrentSongIndexToCarryWithinThisClass, Utils.DPToPX(180,activity!!))
    }

    interface OnReadyCallback{
        fun fragmentReady()
    }
    var callback:OnReadyCallback?= null
}