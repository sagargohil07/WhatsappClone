package com.thinkwik.whatsappclone.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.thinkwik.whatsappclone.R

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class PushNotificationService : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {

        val title = message.notification?.title ?: ""
        val text = message.notification?.body ?: ""
        val icon = message.notification?.icon ?: ""
        val channelId = "MESSAGE"
        val name: CharSequence

        Log.d(
            "push-noti",
            "PushNotificationService : onMessageReceived: ${message.data.toString()}"
        )
        /*  Log.d("push-noti", "PushNotificationService : onMessageReceived: SinchPush.isSinchPushPayload(message.data) ${SinchPush.isSinchPushPayload(message.data)}")*/
        Log.d("push-noti", "PushNotificationService : onMessageReceived: $title $text")

        val channel: NotificationChannel = NotificationChannel(
            channelId,
            "message notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val context  = this

        val notificationBuilder = Notification.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_icon) // Set the notification icon here
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).notify(1, notificationBuilder.build())

        /*if (SinchPush.isSinchPushPayload(message.data)) {
            startService(Intent(this, SinchService::class.java))
            bindService(
                Intent(this, SinchService::class.java),
                object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        if (service is SinchService.SinchClientServiceBinder) {
                            service.sinchClient?.relayRemotePushNotification(
                                SinchPush.queryPushNotificationPayload(applicationContext, message.data))
                        }
                        unbindService(this)
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {}

                },
                Context.BIND_AUTO_CREATE
            )

            val channel: NotificationChannel = NotificationChannel(
                channelId,
                "call notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notificationBuilder = Notification.Builder(this, channelId)
                .setContentTitle("Incoming call")
                .setContentText("user calling you")
                .setSmallIcon(R.drawable.ic_icon) // Set the notification icon here
                .setAutoCancel(true)
            NotificationManagerCompat.from(this).notify(111, notificationBuilder.build())
        }*/

        super.onMessageReceived(message)
    }
}


