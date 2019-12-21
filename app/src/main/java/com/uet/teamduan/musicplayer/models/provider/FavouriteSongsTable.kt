package com.uet.teamduan.musicplayer.models.provider

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.uet.teamduan.musicplayer.models.data.FavouriteSongInfo

private const val TAG = "FAVOURITE_SONGS_TABLE"
class FavouriteSongsTable {

    fun onCreate(db:SQLiteDatabase?){
        Log.d(TAG, "Created table "+FavouriteSongInfo.TABLE_NAME)

        db?.execSQL(FavouriteSongInfo.CREATE_TABLE)
        db?.execSQL(FavouriteSongInfo.CREATE_UNIQUE)
    }
    fun onUpgrade(db:SQLiteDatabase?){
        Log.d(TAG, "Upgraded table "+FavouriteSongInfo.TABLE_NAME)
        db?.execSQL("DROP TABLE IF EXISTS "+FavouriteSongInfo.TABLE_NAME)
        onCreate(db)
    }

    fun insertFavouriteSong(song: FavouriteSongInfo, writableDatabase:SQLiteDatabase):Long{


        val values = ContentValues()

        values.put(FavouriteSongInfo.COLUMN_TITLE, song.title)
        values.put(FavouriteSongInfo.COLUMN_SONG_ID, song.songId)

        val id = writableDatabase.replace(FavouriteSongInfo.TABLE_NAME,null, values)
        Log.d(TAG, "Inserted favourite song " + song.title+" id " +id)
        return id
    }

    fun getFavouriteSong(id:Long,readableDatabase:SQLiteDatabase): FavouriteSongInfo {
        val cursor = readableDatabase.query(
            FavouriteSongInfo.TABLE_NAME,Array(3){ FavouriteSongInfo.COLUMN_TITLE; FavouriteSongInfo.COLUMN_SONG_ID},
            FavouriteSongInfo.COLUMN_SONG_ID+"=?",Array(1){id.toString()},null, null,null, null)

        if(cursor!=null){
            cursor.moveToFirst()
        }
        val song = FavouriteSongInfo(
            cursor.getLong(cursor.getColumnIndex(FavouriteSongInfo.COLUMN_SONG_ID)),
            cursor.getString(cursor.getColumnIndex(FavouriteSongInfo.COLUMN_TITLE)))

        cursor.close()
        return song
    }
    fun getAllFavoriteSongs(writableDatabase:SQLiteDatabase):ArrayList<FavouriteSongInfo>{
        val list = ArrayList<FavouriteSongInfo>()
        val selectQuery = "SELECT * FROM "+ FavouriteSongInfo.TABLE_NAME+" ORDER BY "+ FavouriteSongInfo.COLUMN_TITLE+" DESC"
        val cursor = writableDatabase.rawQuery(selectQuery, null)
        if(cursor.moveToFirst()){
            do{
                val song = FavouriteSongInfo(
                    cursor.getLong(cursor.getColumnIndex(FavouriteSongInfo.COLUMN_SONG_ID)),
                    cursor.getString(cursor.getColumnIndex(FavouriteSongInfo.COLUMN_TITLE)))

                list.add(song)
            }while(cursor.moveToNext())
        }
        writableDatabase.close()
        return list
    }
    fun getFavouriteSongCount(readableDatabase:SQLiteDatabase):Int{
        val countQuery = "SELECT * FROM "+ FavouriteSongInfo.TABLE_NAME
        val cursor = readableDatabase.rawQuery(countQuery,null)
        val count = cursor.count
        cursor.close()
        return count
    }
    fun deleteBySongId(songId:Long,writableDatabase:SQLiteDatabase){
        Log.d(TAG, "Deleted favourite song "+songId)
        writableDatabase.delete(FavouriteSongInfo.TABLE_NAME, FavouriteSongInfo.COLUMN_SONG_ID+"= ?",Array(1){songId.toString()})
        writableDatabase.close()
    }

    fun deleteById(id:Long,writableDatabase:SQLiteDatabase){
        writableDatabase.delete(FavouriteSongInfo.TABLE_NAME, FavouriteSongInfo.COLUMN_SONG_ID+"= ?",Array(1){id.toString()})
        writableDatabase.close()
    }
}