package com.uet.teamduan.musicplayer.screens.adapters

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

class SimpleItemTouchHelperCallback(
    var adapter:ItemTouchHelperAdapter
): ItemTouchHelper.Callback() {


    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }


    override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN,ItemTouchHelper.START or ItemTouchHelper.END)
    }

    override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
        adapter.onItemMove(p1.adapterPosition,p2.adapterPosition)
        return true
    }

    override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {

    }


    interface ItemTouchHelperAdapter{
        fun onItemMove(fromPos:Int,toPos:Int)
//        fun onItemDimiss(pos:Int)
    }
}