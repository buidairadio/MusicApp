package com.uet.teamduan.musicplayer.models.tasks

import android.content.Context
import android.os.AsyncTask
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Category
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.models.provider.LocalSongProvider
import com.uet.teamduan.musicplayer.utils.scaleAndCropCenter
//
//Pair<Context,
//Pair<Pair<ArrayList<Song>,LinkedHashMap<Long,Int>>,Pair<>>
//>Pair<Context,Pair<ArrayList<Song>,LinkedHashMap<Long,Int>>>
class LoadSongDataAsyncTask(
    private var callback:SongDataCallback

):AsyncTask<LoadSongDataAsyncTask.Params,
        Pair<ArrayList<Song>?,Pair<Int,Song>?>,
        Int>() {

    class Params(
        var context:Context,
        var songList:ArrayList<Song>,
        var songMap:LinkedHashMap<Long,Int>,
        var albumList:ArrayList<Category>,
        var artistList:ArrayList<Category>
        ){}


    override fun doInBackground(vararg params:Params ): Int {
        LocalSongProvider.loadSong(params[0].context,params[0].songList,params[0].songMap,params[0].albumList,params[0].artistList)
        publishProgress(Pair(params[0].songList, null))
        getThumbailsAndLikeStates(params[0].context,params[0].albumList,params[0].artistList,params[0].songMap)
        return 0
    }

    override fun onPostExecute(result: Int?) {
        this.cancel(true)
    }

    override fun onProgressUpdate(vararg values: Pair<ArrayList<Song>?,Pair<Int,Song>?>?) {
        val v = values[0]
        if(v!!.first!=null){
            //come here on finish loading song
            callback.onSongsLoaded(v!!.first!!)
        }else{
            callback.onThumbailsLoaded()
        }
    }




















    private fun getThumbailsAndLikeStates(context:Context, albumList:ArrayList<Category>,artistList:ArrayList<Category>, songMap:LinkedHashMap<Long,Int>) {
        val size = context.resources.getDimensionPixelSize(R.dimen._100sdp)
        for (album in albumList) {
            try {
                val thumb = LocalSongProvider.getThumbail(context, album.id)
                album.art = thumb.scaleAndCropCenter(size, size)
                for(song in album.songList) {
                    song.thumb = album.art
                    publishProgress(Pair(null, Pair(songMap[song.id]!!, song)))
                }
            } catch (e: Exception) {
            }
        }
        for (artist in artistList) {
            try {
                val thumb = LocalSongProvider.getThumbail(context, artist.id)
                artist.art = thumb.scaleAndCropCenter(size, size)
            } catch (e: Exception) {
            }
        }
    }

    interface SongDataCallback{
        fun onThumbailsLoaded()
        fun onSongsLoaded(songList:ArrayList<Song>)
    }
}