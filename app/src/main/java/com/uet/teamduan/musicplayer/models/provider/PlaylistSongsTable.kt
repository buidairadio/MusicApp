package com.uet.teamduan.musicplayer.models.provider

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.uet.teamduan.musicplayer.models.data.PlaylistSongInfo

private const val TAG = "PLAYLIST_SONGS_TABLE"
class PlaylistSongsTable{

    fun onCreate(db:SQLiteDatabase?){
        Log.d(TAG, "Created table "+ PlaylistSongInfo.TABLE_NAME)

        db?.execSQL(PlaylistSongInfo.CREATE_TABLE)
        db?.execSQL(PlaylistSongInfo.CREATE_UNIQUE)
    }
    fun onUpgrade(db:SQLiteDatabase?){
        Log.d(TAG, "Upgraded table "+ PlaylistSongInfo.TABLE_NAME)
        db?.execSQL("DROP TABLE IF EXISTS "+ PlaylistSongInfo.TABLE_NAME)
        onCreate(db)
    }



    fun insertPlaylistSong(song: PlaylistSongInfo,writableDatabase:SQLiteDatabase):Long{

        Log.d(TAG, "Inserted to playlist (song " + song.songId+", playlist "+song.playlistId+")")

        val values = ContentValues()

        values.put(PlaylistSongInfo.COLUMN_PLAYLIST_ID, song.playlistId)
        values.put(PlaylistSongInfo.COLUMN_SONG_ID, song.songId)
        values.put(PlaylistSongInfo.COLUMN_PLAYLIST_TITLE, song.playlistTitle)

        return writableDatabase.replace(PlaylistSongInfo.TABLE_NAME,null, values)
    }

    fun getPlaylistSong(id:Long,readableDatabase:SQLiteDatabase): PlaylistSongInfo {
        val cursor = readableDatabase.query(
            PlaylistSongInfo.TABLE_NAME,Array(2){ PlaylistSongInfo.COLUMN_SONG_ID; PlaylistSongInfo.COLUMN_PLAYLIST_ID},
            PlaylistSongInfo.COLUMN_SONG_ID+"=?",Array(1){id.toString()},null, null,null, null)

        if(cursor!=null){
            cursor.moveToFirst()
        }
        val song = PlaylistSongInfo(
            cursor.getLong(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_SONG_ID)),
            cursor.getLong(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_PLAYLIST_ID)),
            cursor.getString(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_PLAYLIST_TITLE)))

        cursor.close()
        return song
    }

    fun getSongListByPlaylistId(playlistId:Long,readableDatabase: SQLiteDatabase):ArrayList<PlaylistSongInfo>{
        val list = ArrayList<PlaylistSongInfo>()
        val selectQuery = "SELECT * FROM "+ PlaylistSongInfo.TABLE_NAME+" WHERE "+PlaylistSongInfo.COLUMN_PLAYLIST_ID+" = "+playlistId+" ORDER BY "+ PlaylistSongInfo.COLUMN_PLAYLIST_ID+" DESC"
        val cursor = readableDatabase.rawQuery(selectQuery, null)
        if(cursor.moveToFirst()){
            do{
                val song = PlaylistSongInfo(
                    cursor.getLong(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_SONG_ID)),
                    cursor.getLong(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_PLAYLIST_ID)),
                    cursor.getString(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_PLAYLIST_TITLE)))

                list.add(song)
            }while(cursor.moveToNext())
        }
        readableDatabase.close()
        return list
    }

    fun getAllPlaylistSongs(readableDatabase: SQLiteDatabase):ArrayList<PlaylistSongInfo>{
        val list = ArrayList<PlaylistSongInfo>()
        val selectQuery = "SELECT * FROM "+ PlaylistSongInfo.TABLE_NAME+" ORDER BY "+ PlaylistSongInfo.COLUMN_PLAYLIST_ID+" DESC"
        val cursor = readableDatabase.rawQuery(selectQuery, null)
        if(cursor.moveToFirst()){
            do{
                val song = PlaylistSongInfo(
                    cursor.getLong(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_SONG_ID)),
                    cursor.getLong(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_PLAYLIST_ID)),
                    cursor.getString(cursor.getColumnIndex(PlaylistSongInfo.COLUMN_PLAYLIST_TITLE)))

                list.add(song)
            }while(cursor.moveToNext())
        }
        readableDatabase.close()
        return list
    }
    fun getPlaylistSongCount(readableDatabase:SQLiteDatabase):Int{
        val countQuery = "SELECT * FROM "+ PlaylistSongInfo.TABLE_NAME
        val cursor = readableDatabase!!.rawQuery(countQuery,null)
        val count = cursor.count
        cursor.close()
        return count
    }

    fun deleteBySongIdAndPlaylistId(songId:Long, playlistId:Long,writableDatabase:SQLiteDatabase){
        Log.d(TAG, "Deleted song "+
            writableDatabase.delete(PlaylistSongInfo.TABLE_NAME,
                PlaylistSongInfo.COLUMN_SONG_ID+"="+songId+" and "+PlaylistSongInfo.COLUMN_PLAYLIST_ID+"="+playlistId,null))
        writableDatabase.close()
    }

    fun deleteSongsByPlaylistId(id:Long,writableDatabase:SQLiteDatabase){
        writableDatabase.delete(PlaylistSongInfo.TABLE_NAME, PlaylistSongInfo.COLUMN_PLAYLIST_ID+"= ?",Array(1){id.toString()})
        writableDatabase.close()
    }
}