package com.uet.teamduan.musicplayer.services.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.uet.teamduan.musicplayer.services.MusicPlayerService

class NotificationDismissReceiver:BroadcastReceiver(){
	override fun onReceive(context: Context?, intent: Intent?) {

		//this is the holy place where the service is triggered to close
//		val intent = Intent(context, MusicPlayerService::class.java)
//		intent.action = ServicesActions.STOP_START_SERVICE
//		intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
//		context?.startService(intent)

		MusicPlayerService.stopStartService(context!!)
		Log.d("NOTI_RECEIVER","Stop service")
	}
}