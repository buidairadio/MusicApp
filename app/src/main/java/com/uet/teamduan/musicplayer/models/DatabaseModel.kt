package com.uet.teamduan.musicplayer.models

import android.content.Context
import android.util.Log
import com.uet.teamduan.musicplayer.models.data.FavouriteSongInfo
import com.uet.teamduan.musicplayer.models.data.Playlist
import com.uet.teamduan.musicplayer.models.data.PlaylistSongInfo
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.models.provider.FavouriteSongDatabaseHelper
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.DatabaseContract

private const val TAG = "DATABASE_MODEL"

//SQLite
class DatabaseModel(context: Context): DatabaseContract.Model{

    private val database= FavouriteSongDatabaseHelper(context)

    //DatabaseModel.Model
    override fun insertFavouriteSong(songId: Long, title:String) {
        database.favouriteSongsTable.insertFavouriteSong(FavouriteSongInfo(songId, title),database.writableDatabase)
    }

    override fun deleteFavouriteSong(songId: Long) {
        database.favouriteSongsTable.deleteBySongId(songId,database.writableDatabase)
    }

    override fun getFavouriteSongs(): ArrayList<FavouriteSongInfo> {
        return database.favouriteSongsTable.getAllFavoriteSongs(database.readableDatabase)
    }



    override fun insertPlaylist(playlistTitle: String, songs: ArrayList<Song>):Long {

        val playlistId = database.playlistsTable.insertPlaylist(Playlist(playlistTitle),database.writableDatabase)

        Log.d(TAG,"Inserting playlist "+playlistTitle+" "+playlistId)

        for(song in songs)
            database.playlistSongsTable.insertPlaylistSong(PlaylistSongInfo(song.id, playlistId, playlistTitle),database.writableDatabase)

        Log.d(TAG,"Songs count "+database.playlistSongsTable.getPlaylistSongCount(database.readableDatabase))
        val table = database.playlistSongsTable.getAllPlaylistSongs(database.readableDatabase)

        for(playlist in table){
            Log.d(TAG,"playlist ")
        }
        return playlistId
    }
    override fun updatePlaylist(playlistTitle:String,playlistId: Long):Int{
        return database.playlistsTable.updatePlaylist(playlistId,playlistTitle,database.writableDatabase)
    }

    override fun deltePlaylist(playlistId: Long) {
        database.playlistSongsTable.deleteSongsByPlaylistId(playlistId,database.writableDatabase)
        database.playlistsTable.deleteByPlaylistId(playlistId,database.writableDatabase)
    }

    override fun deleteSongInPlaylist(playlist:Playlist,song:Song){
        Log.d(TAG,"Deleted song (playlist "+playlist.id+", song "+song.id+")")
        database.playlistSongsTable.deleteBySongIdAndPlaylistId(song.id,playlist.id,database.writableDatabase)
    }

    override fun getAllPlaylists(): Pair<LinkedHashMap<Long,Playlist>, ArrayList<PlaylistSongInfo>> {
        val playlists = database.playlistsTable.getAllPlaylists(database.readableDatabase)
        val songs = database.playlistSongsTable.getAllPlaylistSongs(database.readableDatabase)
        return Pair(playlists,songs)
    }

}