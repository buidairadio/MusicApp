package com.uet.teamduan.musicplayer.models

import android.content.Context
import com.uet.teamduan.musicplayer.models.data.Category
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.models.tasks.LoadSongDataAsyncTask
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.MusicsLoadingContract

private const val TAG = "MUSIC_LOADING_MODEL"

//content provider
class MusicLoadingModel(var context: Context):MusicsLoadingContract.Model{


    //MusicsLoadingContract.Model
    override fun getSong(
        songList:ArrayList<Song>,

                         songMap:LinkedHashMap<Long,Int>,
                         albumList:ArrayList<Category>, artistList:ArrayList<Category>,

        callback: LoadSongDataAsyncTask.SongDataCallback){


        LoadSongDataAsyncTask(callback).execute(LoadSongDataAsyncTask.Params(context,songList, songMap,albumList,artistList))
    }
}