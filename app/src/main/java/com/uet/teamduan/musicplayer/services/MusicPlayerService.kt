package com.uet.teamduan.musicplayer.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.services.notifications.MusicPlayerNotification
import com.uet.teamduan.musicplayer.tasks.ProgressBarThread
import com.uet.teamduan.musicplayer.visual_effects.WaveformVisualizer
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import android.media.AudioFocusRequest
import android.media.AudioAttributes
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.DatabasePresenter


private val TAG = "MUSIC_PLAYER_SERVICE"
@Suppress("DEPRECATION")
class MusicPlayerService:
    Service()

    , MediaPlayer.OnPreparedListener
    , MediaPlayer.OnCompletionListener
    , ProgressBarThread.ProgressCallback

{




    companion object{
        var serviceBound:Boolean = false
        var serviceRunning:Boolean = false
        var serviceStarted:Boolean = false

        var openActivityFromNotification:Boolean = false
        var shuffleState:Boolean = false
        var loopState:Boolean = false

        var musicPlayerGlobalState:Boolean = false

        fun stopStartService(context:Context){
            val intent = Intent(context, MusicPlayerService::class.java)
            intent.action = ServicesActions.STOP_START_SERVICE
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            context?.startService(intent)
        }
    }
    inner class MusicPlayerBinder: Binder(){
        fun getServiceInstance():MusicPlayerService {
            return this@MusicPlayerService
        }
    }
    var binder:MusicPlayerBinder? = null



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId)
        Log.d(TAG,"Do something with this command "+intent?.action+" flag "+flags+" startId "+startId)
        serviceStarted = true
        when(intent?.action){
            ServicesActions.STOP_START_SERVICE->{
                stopSelf()//dng service
                serviceStarted = false
                musicPlayerNotification = null
            }
            ServicesActions.PLAY_PAUSE->{
                //because to have this notification trigger event
                //the service already played i.e currentSongIndex != -1
                if(musicPlayer!!.isPlaying)
                    pauseMusicPlayer()
                else {
                    playMusicPlayer(currentSongIndex,true, false)
                }
            }
            ServicesActions.PREV->{
                prevSong()
            }
            ServicesActions.NEXT->{
                //Log.d(TAG,"PLAYING next song")
                nextSong()
            }
            ServicesActions.LOVE->{
                //trigger someone from here
                likeSong()
            }

        }
        return START_NOT_STICKY
    }
    //we allow humans to bind to this service
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG,"Binding babe")
        if(binder==null){
            binder = MusicPlayerBinder()
        }
        return binder
    }


    override fun onDestroy() {
        Log.d(TAG,"Service destroyed")
        serviceRunning = false
        musicPlayer?.stop()
        progressBarThread?.running = false
        progressBarThread?.join()
        waveformVisualizer.stopVisualizer()
        musicPlayerGlobalState = false
        super.onDestroy()
    }




    lateinit var context:Context
    var musicPlayerNotification:MusicPlayerNotification? = null
    var musicPlayer: MediaPlayer? = null
    var songList = ArrayList<Song>()
    var isOnline:Boolean = false
    var currentSongIndex:Int = 0
    private var readyForChangeSong:Boolean = true
    var callback:UpdateViewCallback?= null
    private var progressBarThread: ProgressBarThread? = null

    var queuePointer:String = ""
    var queuePointer2:Int = -1


    val waveformVisualizer = WaveformVisualizer()
    var initSongQueueFlag:Boolean = false

    var mAudioManager:AudioManager? = null
    var databasePresenter:DatabasePresenter? = null
    //after this point we use an instance of this class to access from the main activity
    //it is the same as one single separate object created from this class
    //since we cannot pass the parameters into the constructor
    fun initServiceObject(context:Context){
        musicPlayerNotification = MusicPlayerNotification(context,this)
        this.context = context
        //thread exist parallel with the service
        progressBarThread = ProgressBarThread(this)
        progressBarThread?.start()

        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun initSongQueue(list:ArrayList<Song>){
        initSongQueueFlag = true
        songList.clear()
        for(song in list) songList.add(song)
    }

    //this funtion is called when:
        //1. the activity has Stopped and the mediaPlayer pauseMusicPlayer event is triggered
        //2. the the mediaPlayer has paused and the activity stop event is triggered

    fun stopForeground(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            musicPlayerNotification!!.stopForeground()
            Log.d(TAG, "Stopped foreground " + Build.VERSION.SDK_INT+" "+Build.VERSION_CODES.O)
        }
    }
    fun startForeground(){
        if(musicPlayerNotification!=null) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                musicPlayerNotification!!.startForeground()
                Log.d(TAG, "Start foreground " + Build.VERSION.SDK_INT+" "+Build.VERSION_CODES.O)
            }
        }
    }

    //this method is called from Thread
    override fun updateProgress():Float{
        var progress = 0.0f
        if(songList.size>0&&currentSongIndex<songList.size) {
            val maxTime = songList[currentSongIndex].duration
            val currentTime = musicPlayer!!.currentPosition

            progress = (currentTime.toFloat()) / (maxTime.toFloat())

            callback?.updateProgressBar(progress, maxTime.toInt())
        }
        return progress
    }

    //call this from out side
    fun setProgress(progress:Float){
        musicPlayer?.seekTo((progress*songList[currentSongIndex].duration.toFloat()).toInt())
    }





    //control the media player
    //if the first song played then we simply playMusicPlayer it
    //if the current song is played, then we do nothing
    //if the passed in song is different from the current song then we playMusicPlayer that new song
    //and don't forget to stop it if it is running
    fun playMusicPlayer(index:Int, fromStateButton:Boolean, extraCondition:Boolean, shuffleNext:Boolean = false){

        if(!mPlayOnAudioFocus){
            requestAudioFocus()
        }


        if(songList.size>0&&index<songList.size){

            var condition = initSongQueueFlag||currentSongIndex!=index||extraCondition
            if(initSongQueueFlag)
                Log.d(TAG,"initSongQueueFlag")
            if(currentSongIndex!=index)
                Log.d(TAG,"currentSongIndex!=index")
            if(!condition)
                if(currentSongIndex==index) {
                    Log.d(TAG,"currentSongIndex==index")
                    if ((songList[currentSongIndex] != songList[index]) || (loopState&&!fromStateButton)){
                        Log.d(TAG,"songList[currentSongIndex] != songList[index]) || shuffleState == 1")
                        condition = true
                    }
                }

            if(condition){
                initSongQueueFlag = false
                if(!readyForChangeSong){
                    Log.d(TAG,"Not able to enter playing music due to too fast")
                    return
                }
                readyForChangeSong = false
                Log.d(TAG,"Enter playing music")
                //here new song come in
                //if there exist a song was playing we must stop it
                if(musicPlayer!=null) {
                    if (musicPlayer?.isPlaying!!) {
                        musicPlayer?.reset()
                    }else{
                        startForeground()
                    }
                }


                callback?.updateViewOnNewSong(true,songList[index],index,currentSongIndex,shuffleNext)
                if(currentSongIndex<songList.size){
                    songList[currentSongIndex].isPlaying = false
                }

                currentSongIndex = index
                songList[currentSongIndex].isPlaying = true

                musicPlayer = MediaPlayer()
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    musicPlayer!!.setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                }else
                    musicPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

                if(isOnline)
                    musicPlayer!!.setDataSource(getCurrentSong().path)
                else {
                    val uri: Uri = Uri.fromFile(File(getCurrentSong().path))
                    musicPlayer!!.setDataSource(context.applicationContext, uri)
                }
                musicPlayer!!.setOnPreparedListener(this@MusicPlayerService)
                musicPlayer!!.setOnCompletionListener(this@MusicPlayerService)
                musicPlayer!!.prepareAsync()

                waveformVisualizer.startVisualizer(musicPlayer!!.audioSessionId)

            }else{
                //here the same song index is passed in
                //there is 2 possible situations: paused song,or still playing song
                if(!musicPlayer?.isPlaying!!){
                    musicPlayer?.start()
                    startForeground()
                    Log.d(TAG,"Resume playing and start foreground again")
                }//else ignore it
                    callback?.updateViewOnStateChange(true,songList[index],index)
            }

            musicPlayerNotification?.updateNotificationView(getCurrentSong(), true,false)
        }
        musicPlayerGlobalState = true
    }


    //chu cua search box -> trang
    //margin top trong search screen

    override fun onPrepared(mp: MediaPlayer?) {
        try {
            musicPlayer?.start()
        }catch (e:IllegalArgumentException){
            Log.d(TAG,"3 Something went wrong here.")
            e.printStackTrace()
        }catch(e:IllegalStateException){
            Log.d(TAG,"3 Something went wrong here.")
            e.printStackTrace()
        }catch (e: IOException){
            Log.d(TAG,"3 Something went wrong here.")
            e.printStackTrace()
        }


        Log.d(TAG,"Start new song and run thread")
        //this is the time when the media player is ready for changing song
        Handler().postDelayed(object: Runnable{
            override fun run(){
                readyForChangeSong = true
                Log.d(TAG,"On thread finish: ready for change song")
            }
        }, 100)
    }
    override fun onCompletion(mp: MediaPlayer?) {
        //auto next
        nextSong(true)
    }

    //simply pauseMusicPlayer the mediaplayer
    //please make sure that you call this function when the song is playing
    fun pauseMusicPlayer(){
        Log.d(TAG,"Paused")
        //on pauseMusicPlayer we trigger the stopForeground()
        stopForeground()
        //and pauseMusicPlayer the music
        musicPlayer?.pause()
        callback?.updateViewOnStateChange(false,songList[currentSongIndex],currentSongIndex)
        musicPlayerNotification?.updateNotificationView(songList[currentSongIndex],false,false)
        musicPlayerGlobalState = false
    }
    //simply call the next song by giving the right index
    fun nextSong(nextSongOnCompleted:Boolean = false){
        if(!readyForChangeSong)return

        Log.d(TAG,"PLAYING next song "+songList.size)
        var nextSongIndex = getNextSongIndex(nextSongOnCompleted)
        if(nextSongIndex>=songList.size) {
            nextSongIndex -= songList.size
            callback?.onJumpFromOneEndToTheOther()
        }

        playMusicPlayer(nextSongIndex,false,(songList.size==1), shuffleState)
    }
    //simply call the next song by giving the right index
    fun prevSong(){
        if(!readyForChangeSong)return
        Log.d(TAG,"PLAYING previous song")

        var prevSongIndex = currentSongIndex-1

        if(prevSongIndex<=-1) {
            prevSongIndex += songList.size
            callback?.onJumpFromOneEndToTheOther()
        }
        playMusicPlayer(prevSongIndex,false, (songList.size==1))
    }

    fun likeSong(){
        Log.d(TAG,"Love a song from notification")
        if(isOnline){
            Toast.makeText(applicationContext,"Not support like for online song",Toast.LENGTH_SHORT).show()
        }else
            databasePresenter?.likeOrUnlike(songList[currentSongIndex])
        if(callback == null)
            Log.d(TAG,"Callback is nulllllllll")
        if(!serviceBound)
            updateNotificationOnLike(songList[currentSongIndex])
    }
    fun updateNotificationOnLike(song:Song){
        if(song==songList[currentSongIndex])
            musicPlayerNotification?.updateNotificationView(songList[currentSongIndex],musicPlayer?.isPlaying!!,false)
    }
    fun getCurrentSong():Song{
        return songList[currentSongIndex]
    }

    interface UpdateViewCallback{
        fun updateViewOnNewSong(state:Boolean,song:Song,newSongIndex:Int, oldSongIndex:Int, shuffleNext:Boolean){
            updateViewOnStateChange(state,song,newSongIndex)
        }
        fun updateViewOnStateChange(state:Boolean,song:Song, index:Int)
        //range 0.0f-1.0f
        fun updateProgressBar(progress:Float, maxTime:Int)

        fun onJumpFromOneEndToTheOther()
    }

    fun getNextSongIndex(nextSongOnCompleted:Boolean):Int {
        return if (nextSongOnCompleted) {
            if (loopState)
                 currentSongIndex
            else {
                if (shuffleState) {
                     getRandom()
                }else{
                    currentSongIndex+1
                }
            }
        }else{
            if (shuffleState) {
                getRandom()
            }else{
                currentSongIndex+1
            }
        }
    }
    private fun getRandom():Int{
        if(songList.size<=1)return 0
        var randomIndex:Int
        do{
            randomIndex = (Math.random()*(songList.size).toFloat()).toInt()
        }while(randomIndex == currentSongIndex)
        return randomIndex
    }



    val stopMusicHandler = Handler()
    val stopMusicRunnable = object:Runnable{
        override fun run(){
            if(musicPlayer!!.isPlaying){
                pauseMusicPlayer()
            }
            musicPlayerNotification?.dismissNotification()
            Toast.makeText(context,"Musics stopped",Toast.LENGTH_SHORT).show()
        }
    }





    private var mAudioFocusPlaybackDelayed:Boolean = false
    private var mPlayOnAudioFocus:Boolean = false
    private var mLossFocusLastTime:Boolean = false
    private fun play(){
        playMusicPlayer(currentSongIndex,true,false)
    }
    private fun isPlaying():Boolean{
        return musicPlayer!!.isPlaying
    }
    private fun pause(){
        if(mPlayOnAudioFocus) {
            mAudioManager!!.abandonAudioFocus(audioFocusListener)
        }
        pauseMusicPlayer()
    }
    private fun stop(){
        if(mPlayOnAudioFocus)
            mAudioManager!!.abandonAudioFocus(audioFocusListener)
        pauseMusicPlayer()
    }







    private fun requestAudioFocus() {
        val audioFocus:Int?

        Log.d(TAG,"Request audio focus "+Build.VERSION.SDK_INT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build()
            audioFocus = mAudioManager?.requestAudioFocus(focusRequest)
        } else {
            audioFocus = mAudioManager?.requestAudioFocus(
                audioFocusListener, AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }

        when (audioFocus) {
            AudioManager.AUDIOFOCUS_REQUEST_FAILED->{ }
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED->{ }
            AudioManager.AUDIOFOCUS_REQUEST_DELAYED->{
                mAudioFocusPlaybackDelayed = true
            }
        }
    }

    val audioFocusListener = object: AudioManager.OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (!mPlayOnAudioFocus&&mLossFocusLastTime) {
                        play()
                        mLossFocusLastTime = false
                    }
                    mPlayOnAudioFocus = true
                    Log.d(TAG,"FOCUS GAIN")
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> { }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (isPlaying()) {
                    mPlayOnAudioFocus = false
                    mLossFocusLastTime = true
                    pause()
                    Log.d(TAG,"FOCUS LOSSSSSSSS: AUDIOFOCUS_LOSS_TRANSIENT")
                }
                AudioManager.AUDIOFOCUS_LOSS -> if (isPlaying()){
                    mPlayOnAudioFocus = false
                    mLossFocusLastTime = true
                    Log.d(TAG,"FOCUS LOSSSSSSSS: AUDIOFOCUS_LOSS")
                    pause()
                }
            }
        }
    }



}

/*
* the story of service:
*
* when user clicks on the first song for playing. The service will be start in the Start Mode
* at the same time, the activity will be bound to the service.
*
*
* The service keeps playing music until the mediaPlayer is paused and the notification is swiped away
* At that point the service will be destroyed and the static flag will be set to false
*
* */

/*
* the story of media player:
* when the user clicks on the playMusicPlayer button, it trigger the media player to playMusicPlayer
* */

//  to stop the foreground you have to do something
//    //this is a demo to stop the foreground
//    service?.musicPlayerNotification!!.stopForeground()

//if we pauseMusicPlayer and nothing is bound to the service then we can destroy the service

/*
* The story of the media player:
*
* the media player is created when there is a song to playMusicPlayer
* otherwise it will not exist. the question is what if we access the interface while there is no song in the media player
*
* when we press playMusicPlayer for the first time the mediaplayer is created. from that time on ward we can access it
* the assumption is that. no stop . no destroy if playing
* */