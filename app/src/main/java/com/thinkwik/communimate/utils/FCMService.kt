package com.thinkwik.communimate.utils

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.auth.oauth2.GoogleCredentials
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

object FCMService {

    fun sendNotification(context: Context, token: String, title: String, body: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getAccessToken(context) ?: return@launch

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val jsonNotif = JSONObject().apply {
                    put("title", title)
                    put("body", body)
                }
                val wholeObj = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token)
                        put("notification", jsonNotif)
                    })
                }

                val requestBody = wholeObj.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                Log.d("FCMService", "Response: ${response.body?.string()} ${response.message}")
            } catch (e: Exception) {
                Log.e("FCMService", "Error sending FCM notification: ${e.message}")
            }
        }
    }

    @WorkerThread
    private fun getAccessToken(context: Context): String? {
        return try {
            val assetManager = context.assets
            val inputStream: InputStream = assetManager.open("serviceAccountKey.json")
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"))

            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        } catch (e: IOException) {
            Log.e("FCMService", "Error loading credentials: ${e.message}")
            null
        }
    }
}
