package com.uet.teamduan.musicplayer.models

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.MusicsPlayerMetadataContract
import com.uet.teamduan.musicplayer.utils.Constants.SHARED_PREFERENCE_NAME

//SharedPreferences
class MetadataModel(var context:Context):
    MusicsPlayerMetadataContract.Model {

    var sharedPreferences:SharedPreferences? = null



    override fun saveInt(what:String, value: Int) {
        if(sharedPreferences ==null)
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE)
        sharedPreferences!!.edit().putInt(what,value).apply()
    }

    override fun getInt(what:String): Int {
        if(sharedPreferences ==null)
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE)
        return sharedPreferences!!.getInt(what,0)
    }

}