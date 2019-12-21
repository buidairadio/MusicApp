package com.uet.teamduan.musicplayer.models.data


class Playlist(
    var title:String
) {
    var id:Long = -1
    var songNum:Int = 0
    var songList = ArrayList<Song>()
    companion object{
        var TABLE_NAME ="playlists"

        val COLUMN_ID = "id"
        val COLUMN_TITLE= "playlist_title"

        val CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TITLE + " TEXT)"
        val CREATE_UNIQUE = "CREATE UNIQUE INDEX " + TABLE_NAME+"_song_id_title ON " + TABLE_NAME + "("+ COLUMN_TITLE+")"
    }

}