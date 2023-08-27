package com.example.bobmukjaku

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        Log.i("fcmmessage", "received")
        Log.i("fcmmessage" , message.toString())
        Log.i("fcmmessage", "data는 ${message.data["roomId"]}")

        //notification 채널 세팅
        val channelId = "FCM Notification"
        val channelName = "밥먹자쿠"
        val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE


        //펜딩인텐트 생성
        val newIntent = Intent(this, GiveScoreActivity::class.java)
        newIntent.putExtra("roomId", message.data["roomId"]?.toLong())
        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //notification builder 생성
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ku_3)
            .setContentTitle("밥먹자쿠")
            .setContentText("식사는 어떠셨나요?")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //notification 생성
        val notification = builder.build()

        //notification 채널 생성
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.notify(1, notification)
    }


}


