package com.uet.teamduan.musicplayer.models.data


class FavouriteSongInfo(var songId:Long, var title:String) {
  companion object{
    var TABLE_NAME ="favourite_songs"

    val COLUMN_ID = "id"
    val COLUMN_TITLE = "title"
    val COLUMN_SONG_ID = "song_id"

    val CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_SONG_ID + " INTEGER," + COLUMN_TITLE + " TEXT)"
    val CREATE_UNIQUE = "CREATE UNIQUE INDEX " + TABLE_NAME+"_song_id_title ON " + TABLE_NAME + "("+ COLUMN_SONG_ID+","+ COLUMN_TITLE+")"
  }
}
