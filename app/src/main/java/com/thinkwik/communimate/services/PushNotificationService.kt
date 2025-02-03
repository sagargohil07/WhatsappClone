package com.thinkwik.communimate.services

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
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.thinkwik.communimate.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Collections

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class PushNotificationService : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: "New Message"
        val text = message.notification?.body ?: "You have a new notification"
        val channelId = "MESSAGE"

        Log.d("push-noti", "PushNotificationService : onMessageReceived: ${message.data}")

        val channel = NotificationChannel(
            channelId,
            "Message Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notificationBuilder = Notification.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_icon) // Ensure correct icon is set
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(1, notificationBuilder.build())

        super.onMessageReceived(message)
    }

    fun sendNotification(context: Context, token: String, title: String, body: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getAccessToken(context) ?: return@launch

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val jsonNotif = JSONObject().apply {
                    put("title", title)
                    put("body", body)
                }
                val message = JSONObject().apply {
                    put("token", token)
                    put("notification", jsonNotif)
                }
                val requestBody = JSONObject().apply {
                    put("message", message)
                }.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/whatapp-45b1a/messages:send")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                Log.d("push-noti", "Response: ${response.body?.string()} ${response.message}")
            } catch (e: Exception) {
                Log.e("push-noti", "Error sending FCM notification: ${e.message}")
            }
        }
    }

    private fun getAccessToken(context: Context): String? {
        return try {
            val inputStream: InputStream = context.assets.open("serviceAccountKey.json")
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"))

            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        } catch (e: IOException) {
            Log.e("push-noti", "Error loading credentials: ${e.message}")
            null
        }
    }
}
