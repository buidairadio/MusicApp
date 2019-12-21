package com.uet.teamduan.musicplayer.screens.HomeScreenActivity

import com.uet.teamduan.musicplayer.models.data.Category
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.models.tasks.LoadSongDataAsyncTask

class MusicsLoadingPresenter(
    var view:MusicsLoadingContract.View,
    var model:MusicsLoadingContract.Model
) :MusicsLoadingContract.Presenter{

    override fun loadSong(songList:ArrayList<Song>, songMap:LinkedHashMap<Long, Int>,
                          albumList:ArrayList<Category>, artistList:ArrayList<Category>) {
        //assume that songs are loaded

        model.getSong(songList,songMap,albumList,artistList,

            object:LoadSongDataAsyncTask.SongDataCallback{
                override fun onThumbailsLoaded() {
                    view.onLoadedThumbails()
                }

                override fun onSongsLoaded(songList:ArrayList<Song>) {
                    if(songList.size==0)
                        view.showErrorMessage()
                    else
                        view.onLoadedSongList()
                }
        })
    }


}