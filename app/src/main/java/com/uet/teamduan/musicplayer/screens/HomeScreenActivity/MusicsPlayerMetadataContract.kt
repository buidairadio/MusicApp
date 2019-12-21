package com.uet.teamduan.musicplayer.screens.HomeScreenActivity

class MusicsPlayerMetadataContract {
    interface View{
        fun updateOnShuffleStateChange(state:Boolean)
        fun updateOnLoopStateChange(state:Boolean)
    }

    interface Presenter{
        fun changeLoopState()
        fun changeShuffleState()
        fun getShuffleState()

        fun saveTimer(time:Int)
        fun getPreviousTimer():Int

        fun saveDarkmode(darkModeEnabled:Boolean)
        fun getDarkMode():Boolean
    }

    //shared preference
    interface Model{
        fun saveInt(what:String,value:Int)
        fun getInt(what:String):Int
    }
}