package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.*
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Playlist
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity
import com.uet.teamduan.musicplayer.screens.SongSelectActivity
import com.uet.teamduan.musicplayer.screens.adapters.PlaylistRecyclerViewAdapter

private const val TAG = "PLAYLIST_FRAGMENT"

class PlaylistFragment: Fragment() {

    private val REQUEST_SONG_SELECT_CODE = 123

    private var newPlaylistTitle:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return initView(inflater.inflate(R.layout.fragment_playlist, container, false))
    }

    private fun initView(view :View):View{
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_playlist_fragment)
        val fabAddPlaylist = view.findViewById<CardView>(R.id.cv_add_playlist)

        fabAddPlaylist.visibility = View.VISIBLE

        fabAddPlaylist.setOnClickListener {
            Log.d(TAG, "Create playlist button pressed")
            createDialog()
        }


        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        val gridLayoutManager= GridLayoutManager(activity,2)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = (activity as HomeScreenActivity).playlistAdapter
        return view
    }


    fun createDialog(){
        var builder = AlertDialog.Builder(activity)
        builder.setView(layoutInflater.inflate(R.layout.enter_name_dialog,null))
        val dialog = builder.create()
        dialog.show()
        val title = dialog.findViewById<EditText>(R.id.et_enter_name_dialog)
        dialog.findViewById<CardView>(R.id.cv_enter_name_dialog_ok).setOnClickListener {
            Log.d(TAG, "Dialog returned "+title.text)
            newPlaylistTitle = title.text.toString()
            if(newPlaylistTitle==""){
                Toast.makeText(activity,"Empty name",Toast.LENGTH_SHORT).show()
            }else {
                //enter the select song fragment from here...
                startSongSelectingActivityForResult()
            }
            dialog.dismiss()
        }
        dialog.findViewById<CardView>(R.id.cv_enter_name_dialog_cancel).setOnClickListener {
            dialog.cancel()
        }
    }
    companion object{
        fun createDialogToRename(context: Context, playlist: Playlist, index:Int, adapter:PlaylistRecyclerViewAdapter,inflater:LayoutInflater){
            var builder = AlertDialog.Builder(context)
            builder.setView(inflater.inflate(R.layout.enter_name_dialog,null))

            val dialog = builder.create()
            dialog.show()

            val title = dialog.findViewById<EditText>(R.id.et_enter_name_dialog)
            title.setText(playlist.title)
            title.setSelection(title.text.length)
            dialog.findViewById<CardView>(R.id.cv_enter_name_dialog_ok).setOnClickListener {
                Log.d(TAG, "Dialog returned "+title.text)

                val newPlaylistTitle = title.text.toString()
                if(newPlaylistTitle==""){
                    Toast.makeText(context,"Empty name",Toast.LENGTH_SHORT).show()
                }else {
                    playlist.title = title.text.toString()
                    (context as HomeScreenActivity).databasePresenter.renamePlaylist(newPlaylistTitle,playlist.id)
                }
                dialog.dismiss()
            }
            dialog.findViewById<CardView>(R.id.cv_enter_name_dialog_cancel).setOnClickListener {
                dialog.cancel()
            }

        }
    }

    private fun startSongSelectingActivityForResult(){
        val intent = Intent(activity, SongSelectActivity::class.java)

        intent.putParcelableArrayListExtra("songList",(activity as HomeScreenActivity).songList)
        intent.putExtra("dark_mode_enabled",HomeScreenActivity.darkModeEnabled)

        startActivityForResult(intent,REQUEST_SONG_SELECT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_SONG_SELECT_CODE&&resultCode== Activity.RESULT_OK){
            val selectedSongs = data?.getParcelableArrayListExtra<Song>("selectedSongs")
            Log.d(TAG, "Selected "+selectedSongs?.size+" songs")
            (activity as HomeScreenActivity).databasePresenter.createPlaylist(newPlaylistTitle!!,selectedSongs!!)
        }
    }
}