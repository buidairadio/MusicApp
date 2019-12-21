package com.uet.teamduan.musicplayer.screens.HomeScreenActivity

import com.uet.teamduan.musicplayer.models.data.Playlist
import com.uet.teamduan.musicplayer.models.data.Song

private const val TAG = "DATABASE_PRESENTER"
class DatabasePresenter(
    var view: DatabaseContract.View,
    var model:DatabaseContract.Model):DatabaseContract.Presenter {



    override fun likeOrUnlike(song: Song) {
        if(!song.liked) {
            song.liked = true
            model.insertFavouriteSong(song.id,song.title)
            view.updateOnLike(song)
        }
        else {
            song.liked = false
            model.deleteFavouriteSong(song.id)
            view.updateOnUnlike(song)
        }
    }

    //this function gets called only onCreate
    override fun getFavouriteSongs(favouriteList:ArrayList<Song>, songList:ArrayList<Song>
                                   ,songMap:LinkedHashMap<Long,Int>,favouriteMap:LinkedHashMap<Int,Long>)
    {
        //since this method gets called many times during running time
        //it must be cleared before updating with new data
        favouriteList.clear()
        val list = model.getFavouriteSongs()
        for((index,fs) in list.withIndex()){
//            fs.song = songList[songMap[fs.songId]!!]
            favouriteList.add(songList[songMap[fs.songId]!!])
            favouriteMap.put(index,fs.songId)

            songList[songMap[fs.songId]!!].liked = true
        }
        view.updateOnFavouritSongsLoaded(favouriteList.size)
    }





    override fun createPlaylist(playlistTitle: String, songs: ArrayList<Song>) {
        if(songs.size>0) {
            val id = model.insertPlaylist(playlistTitle, songs)
            if (id > 0) {
                view.showMessageOnPlaylistCreated(id)
            }
        }
    }
    override fun renamePlaylist(playlistTitle:String,playlistId:Long){
        model.updatePlaylist(playlistTitle,playlistId)
        view.showMessageOnPlaylistRenamed(playlistId)
    }

    override fun deletePlaylist(playlist: Playlist) {
        model.deltePlaylist(playlist.id)
        view.updateOnPlaylistsLoaded(-1)
    }
    override fun deleteSongInPlaylist(playlist:Playlist,song:Song){
        model.deleteSongInPlaylist(playlist,song)
        view.showMessageOnDeletedSongFromPlaylist(playlist,song)
    }

    override fun getAllPlaylists(
        playlists: ArrayList<Playlist>,
        playlistSongMaps: ArrayList<LinkedHashMap<Int, Long>>,
        songList: ArrayList<Song>,
        songMap: LinkedHashMap<Long, Int>
    ) {
        playlists.clear()
        val data = model.getAllPlaylists()
        val tempMap = LinkedHashMap<Long,Int>()
        var playlistSongListIndex = 0
        for(songInfo in data.second){
            if(tempMap[songInfo.playlistId]==null){
                tempMap[songInfo.playlistId] = playlistSongListIndex++
                val playlist = data.first[songInfo.playlistId]!!
                playlist.songNum = 1
                playlists.add(playlist)
            }
            val playlist = data.first[songInfo.playlistId]!!
            playlist.songNum++
            playlist.songList.add(songList[songMap[songInfo.songId]!!])

        }
        view.updateOnPlaylistsLoaded(playlists.size)
    }

}