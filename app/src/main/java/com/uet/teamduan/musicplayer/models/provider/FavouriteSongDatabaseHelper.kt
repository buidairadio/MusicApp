package com.uet.teamduan.musicplayer.models.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val TAG = "DATABASE_HELPER"

class FavouriteSongDatabaseHelper(
    var context: Context,
    DATABASE_NAME:String = "s10_music_player_db",
    DATABASE_VERSION:Int = 8
) : SQLiteOpenHelper(context,DATABASE_NAME,null, DATABASE_VERSION){


    val favouriteSongsTable = FavouriteSongsTable()
    val playlistSongsTable = PlaylistSongsTable()
    val playlistsTable = PlaylistsTable()

    override fun onCreate(db: SQLiteDatabase?) {
        favouriteSongsTable.onCreate(db)
        playlistSongsTable.onCreate(db)
        playlistsTable.onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        favouriteSongsTable.onUpgrade(db)
        playlistSongsTable.onUpgrade(db)
        playlistsTable.onUpgrade(db)
    }
}