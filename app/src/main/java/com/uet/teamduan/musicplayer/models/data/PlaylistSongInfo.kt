package com.uet.teamduan.musicplayer.models.data

class PlaylistSongInfo(var songId:Long , var playlistId:Long, var playlistTitle:String) {
    companion object{
        var TABLE_NAME ="playlist_songs"

        val COLUMN_ID = "id"
        val COLUMN_PLAYLIST_ID = "playlist_id"
        val COLUMN_SONG_ID = "song_id"
        val COLUMN_PLAYLIST_TITLE = "playlist_title"

        val CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_SONG_ID + " INTEGER," + COLUMN_PLAYLIST_ID + " INTEGER,"+COLUMN_PLAYLIST_TITLE+" TEXT)"
        val CREATE_UNIQUE = "CREATE UNIQUE INDEX " + TABLE_NAME+"_song_id_title ON " + TABLE_NAME + "("+COLUMN_SONG_ID +","+COLUMN_PLAYLIST_TITLE+")"
    }
}