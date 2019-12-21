package com.uet.teamduan.musicplayer.screens.HomeScreenActivity

import android.util.Log
import com.uet.teamduan.musicplayer.utils.Constants.DARK_MODE_ENABLED
import com.uet.teamduan.musicplayer.utils.Constants.PREVIOUS_TIMER
import com.uet.teamduan.musicplayer.utils.Constants.SHUFFLE_STATE

private const val TAG = "MP_METADATA_PRESENTER"
class MusicsPlayerMetadataPresenter(
    var view:MusicsPlayerMetadataContract.View,
    var model:MusicsPlayerMetadataContract.Model
):MusicsPlayerMetadataContract.Presenter {


    var loopState:Boolean = false
    var shuffleState:Boolean = false



    override fun changeLoopState(){
        loopState = !loopState
        model.saveInt(SHUFFLE_STATE,getStateInteger())
        view.updateOnLoopStateChange(loopState)
    }


    override fun changeShuffleState() {
        shuffleState = !shuffleState
        model.saveInt(SHUFFLE_STATE,getStateInteger())
        view.updateOnShuffleStateChange(shuffleState)
    }

    override fun getShuffleState() {
        getBooleanState(model.getInt(SHUFFLE_STATE))
        view.updateOnShuffleStateChange(shuffleState)
        view.updateOnLoopStateChange(loopState)
    }


    override fun saveTimer(time:Int){
        model.saveInt(PREVIOUS_TIMER,time)
        Log.d(TAG,"timer saved "+time)
    }
    override fun getPreviousTimer():Int{
        val time = model.getInt(PREVIOUS_TIMER)
        Log.d(TAG,"fetched timer "+time)
        return time
    }

    override fun saveDarkmode(darkModeEnabled:Boolean){
        model.saveInt(DARK_MODE_ENABLED,if(darkModeEnabled){1}else{0})
    }
    override fun getDarkMode():Boolean{
        return if(model.getInt(DARK_MODE_ENABLED) == 1){
            true
        }else{
            false
        }
    }






    private fun getStateInteger():Int{
        return if(loopState){
            if(shuffleState) 3
            else 1
        }else{
            if(shuffleState) 2
            else 0
        }
    }
    private fun getBooleanState(currentState:Int){
        when(currentState){
            0->{
                loopState = false
                shuffleState = false
            }
            1->{
                loopState = true
                shuffleState = false
            }
            2->{
                loopState = false
                shuffleState = true
            }
            3->{
                loopState = true
                shuffleState = true
            }

        }
    }
}