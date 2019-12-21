package com.uet.teamduan.musicplayer.services.notifications

import android.app.*

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity
import com.uet.teamduan.musicplayer.services.MusicPlayerService
import com.uet.teamduan.musicplayer.services.ServicesActions
import android.app.NotificationManager
import com.uet.teamduan.musicplayer.utils.getCircleBitmap


//this notification will live along with the service as a loyal slave
const val NOTIFICATION_ID = 144
const val TAG ="MUSIC_PLAYER_NOTI"


class MusicPlayerNotification(
        var context:Context,
        var service: Service
) {
    companion object{
        val INTENT_ACTION_FROM_NOTIFICATION = "from_notification"
        var foregroundRunning:Boolean = false
    }

    private var notification:Notification? = null
    private var remoteViews:RemoteViews
    private var remoteViews2:RemoteViews
    init{
        //here we create the notification
        //call this one time, but more no problem
        createNotificationChannel()
        remoteViews = createRemoteViews()
        remoteViews2 = createRemoteViews()
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, HomeScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val deleteIntent = PendingIntent.getBroadcast(context.applicationContext, 0, Intent(context, NotificationDismissReceiver::class.java), 0)


        val builder = NotificationCompat.Builder(context, context.resources.getString(R.string.CHANNEL_ID))
            .setSmallIcon(R.drawable.ic_noti_status_bar)
            .setCustomBigContentView(updateRemoteViews(false,null))
            .setContent(updateRemoteViews2(null))
            .setContentIntent(pendingIntent)
            .setDeleteIntent(deleteIntent)
            .setOnlyAlertOnce(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
        }

        notification = builder.build()

        if(notification!=null)
            with(NotificationManagerCompat.from(context)) { notify(NOTIFICATION_ID, notification!!) }

        //whenever you init you playMusicPlayer a song
        startForeground()
    }
    fun updateNotificationView(song: Song, state:Boolean, showCloseButton: Boolean){//showCloseButton: Boolean -> Android 4. X
        val intent = Intent(context, HomeScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        }
        intent.action = INTENT_ACTION_FROM_NOTIFICATION

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val deleteIntent = PendingIntent.getBroadcast(context.applicationContext, 0, Intent(context, NotificationDismissReceiver::class.java), 0)

        val builder = NotificationCompat.Builder(context, context.resources.getString(R.string.CHANNEL_ID))
            .setSmallIcon(R.drawable.ic_noti_status_bar)
            .setCustomBigContentView(updateRemoteViews(showCloseButton,song, state))
            .setContent(updateRemoteViews2(song, state))
            .setContentIntent(pendingIntent)
            .setDeleteIntent(deleteIntent)
            .setOnlyAlertOnce(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
        }
        notification = builder.build()
        if(notification!=null)
            with(NotificationManagerCompat.from(context)) { notify(NOTIFICATION_ID, notification!! )}

        Log.d(TAG,"Notification updated "+if(state){"true"}else{"false"})
    }

    fun dismissNotification(){
        with(NotificationManagerCompat.from(context)) { cancel(NOTIFICATION_ID)}
    }


    fun stopForeground(){
        service.stopForeground(false)
        Log.d(TAG,"Stop the foreground")

        foregroundRunning = false
    }
    fun startForeground(){
        service.startForeground(NOTIFICATION_ID,notification)
        Log.d(TAG,"Start the foreground")
        foregroundRunning = true
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.resources.getString(R.string.channel_name)
            val descriptionText = context.resources.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(context.resources.getString(R.string.CHANNEL_ID), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createRemoteViews(): RemoteViews {
        //now we create a remoteViews that has assign all the intent action to the buttons of the notification layout
        //those intents will then be captured by the MusicPlayerService for controlling the mediaPlayer
        val remoteViews = RemoteViews(context.packageName,R.layout.notification_music_player)
        val intent = createPendingIntentWithAction(ServicesActions.PLAY_PAUSE)
        remoteViews.setOnClickPendingIntent(R.id.bt_noti_state,intent)
        remoteViews.setOnClickPendingIntent(R.id.bt_noti_next,createPendingIntentWithAction(ServicesActions.NEXT))
        remoteViews.setOnClickPendingIntent(R.id.bt_noti_prev,createPendingIntentWithAction(ServicesActions.PREV))
        remoteViews.setOnClickPendingIntent(R.id.bt_noti_love,createPendingIntentWithAction(ServicesActions.LOVE))
        return remoteViews
    }

    private fun updateRemoteViews(showCloseButton:Boolean,song:Song?=null, state:Boolean = true):RemoteViews{
        if(song==null)return remoteViews
        remoteViews.setTextViewText(R.id.tv_noti_song_title,song.title)
        if(song.thumb!=null)
            remoteViews.setImageViewBitmap(R.id.iv_noti_thumb, getCircleBitmap(song.thumb!!))
        else
            remoteViews.setImageViewResource(R.id.iv_noti_thumb,R.drawable.ic_songs)

        remoteViews.setImageViewResource(R.id.iv_noti_state,if(state){R.drawable.ic_pause_music_player}else{R.drawable.ic_play_music_player})
        remoteViews.setImageViewResource(R.id.iv_noti_love,if(song.liked){R.drawable.ic_like_click}else{R.drawable.ic_like_white})

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O&&showCloseButton) {
            val deleteIntent = PendingIntent.getBroadcast(context.applicationContext, 0, Intent(context, NotificationDismissReceiver::class.java), 0)
            remoteViews.setViewVisibility(R.id.bt_noti_close,View.VISIBLE)
            remoteViews.setOnClickPendingIntent(R.id.bt_noti_close,deleteIntent)
        }else{
            remoteViews.setViewVisibility(R.id.bt_noti_close,View.GONE)
        }
        return remoteViews
    }

    private fun updateRemoteViews2(song:Song?=null, state:Boolean = true):RemoteViews{
        if(song==null)return remoteViews2
        remoteViews2.setTextViewText(R.id.tv_noti_song_title,song.title)
        remoteViews2.setViewVisibility(R.id.iv_noti_thumb, View.GONE)
        remoteViews2.setImageViewResource(R.id.iv_noti_state,if(state){R.drawable.ic_pause_music_player}else{R.drawable.ic_play_music_player})
        remoteViews2.setImageViewResource(R.id.iv_noti_love,if(song.liked){R.drawable.ic_like_click}else{R.drawable.ic_like_white})
        return remoteViews2
    }

    private fun createPendingIntentWithAction(action:String):PendingIntent{
        val intent = Intent(context, MusicPlayerService::class.java)
        intent.action = action
        return PendingIntent.getService(context,0,intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}