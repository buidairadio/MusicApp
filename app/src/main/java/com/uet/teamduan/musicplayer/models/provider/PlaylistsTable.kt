package com.uet.teamduan.musicplayer.models.provider

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.uet.teamduan.musicplayer.models.data.Playlist

private const val TAG = "PLAYLIST_TABLE"
class PlaylistsTable {

    fun onCreate(db:SQLiteDatabase?){
        Log.d(TAG, "Created table "+ Playlist.TABLE_NAME)

        db?.execSQL(Playlist.CREATE_TABLE)
        db?.execSQL(Playlist.CREATE_UNIQUE)
    }
    fun onUpgrade(db:SQLiteDatabase?){
        Log.d(TAG, "Upgraded table "+ Playlist.TABLE_NAME)
        db?.execSQL("DROP TABLE IF EXISTS "+ Playlist.TABLE_NAME)
        onCreate(db)
    }




    fun insertPlaylist(playlist: Playlist,writableDatabase:SQLiteDatabase):Long{


        val values = ContentValues()

        values.put(Playlist.COLUMN_TITLE, playlist.title)

        val id = writableDatabase.replace(Playlist.TABLE_NAME,null, values)

        if(!id.equals(-1))
            Log.d(TAG, "Inserted playlist " + playlist.title+" id "+id)

        return id
    }
    fun updatePlaylist(playlistId: Long,playlistTitle:String,writableDatabase:SQLiteDatabase):Int{
        val values = ContentValues()
        values.put(Playlist.COLUMN_TITLE, playlistTitle)
        return writableDatabase.update(Playlist.TABLE_NAME, values,Playlist.COLUMN_ID+"=?",Array(1){playlistId.toString()})
    }

    fun getPlaylist(id:Long,readableDatabase:SQLiteDatabase): Playlist {

        val cursor = readableDatabase.query(
            Playlist.TABLE_NAME,Array(3){ Playlist.COLUMN_TITLE; Playlist.COLUMN_ID},
            Playlist.COLUMN_ID+"=?",Array(1){id.toString()},null, null,null, null)

        if(cursor!=null){
            cursor.moveToFirst()
        }

        val playlist = Playlist(cursor.getString(cursor.getColumnIndex(Playlist.COLUMN_TITLE)))
        playlist.id = cursor.getLong(cursor.getColumnIndex(Playlist.COLUMN_ID))

        cursor.close()
        return playlist
    }
    fun getAllPlaylists(writableDatabase:SQLiteDatabase):LinkedHashMap<Long, Playlist>{
        val list = LinkedHashMap<Long,Playlist>()
        val selectQuery = "SELECT * FROM "+ Playlist.TABLE_NAME+" ORDER BY "+ Playlist.COLUMN_TITLE+" DESC"
        val cursor = writableDatabase.rawQuery(selectQuery, null)
        if(cursor.moveToFirst()){
            do{
                val playlist = Playlist(cursor.getString(cursor.getColumnIndex(Playlist.COLUMN_TITLE)))
                playlist.id = cursor.getLong(cursor.getColumnIndex(Playlist.COLUMN_ID))
                list.put(playlist.id,playlist)
            }while(cursor.moveToNext())
        }
        writableDatabase.close()
        return list
    }
    fun getPlaylistCount(readableDatabase:SQLiteDatabase):Int{
        val countQuery = "SELECT * FROM "+ Playlist.TABLE_NAME
        val cursor = readableDatabase!!.rawQuery(countQuery,null)
        val count = cursor.count
        cursor.close()
        return count
    }
    fun deleteByPlaylistId(playlistId:Long,writableDatabase:SQLiteDatabase){
        Log.d(TAG, "Deleted favourite playlist "+playlistId)
        writableDatabase.delete(Playlist.TABLE_NAME, Playlist.COLUMN_ID+"= ?",Array(1){playlistId.toString()})
        writableDatabase.close()
    }

}