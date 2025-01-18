package com.thinkwik.communimate.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.communimate.R
import com.thinkwik.communimate.module.model.FileModel
import com.thinkwik.communimate.module.model.MessageModel
import com.thinkwik.communimate.prefs.PreferenceStorage
import com.thinkwik.communimate.utils.DBHelper
import com.thinkwik.communimate.utils.MediaUploadUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * media upload service for background uploading with notification.
 *
 * Example:
 *  ```
 *  val uploadServiceIntent = Intent(requireActivity(), UploadService::class.java)
 *  uploadServiceIntent.putExtra("mediaType", "")
 *  uploadServiceIntent.putExtra("mediaUrl", "")
 *  uploadServiceIntent.putExtra("audioFileName", "")
 *  uploadServiceIntent.putExtra("senderUid", "")
 *  uploadServiceIntent.putExtra("roomId", "")
 *  uploadServiceIntent.putExtra("name", "")
 *  uploadServiceIntent.putExtra("token", "")
 *  ```
 * @param uploadFor which firestore directory eg.CHAT,CHANNEL UPDATE , STATUS
 * @param mediaType which media passed image,audio,video.
 * @param mediaUrl path of content (image/video/audio) eg.content://media/picker/0/com.android.providers.media.photopicker/media/1000000033
 * @param audioFileName if sending audio file give you audio file nome here.
 * @param senderUid sender id of user
 * @param roomId id between user A,B conversion room
 */
class UploadService : Service() {

    private val prefs: PreferenceStorage by inject()
    private lateinit var dbHelper: DBHelper
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private var uploadFor: String? = null
    private var mediaType: String? = null
    private var mediaUrl: Uri? = null
    private var audioFileName: String? = null
    private var senderUid: String? = null
    private var roomId: String? = null
    private var name: String? = null
    private var token: String? = null
    //channel param
    private var channelName: String? = null
    private var channelImageCaptions: String? = null
    private val notificationId = Random.nextInt()

    override fun onCreate() {
        super.onCreate()
        Log.d("upload-service", "onCreate: ")
        dbHelper = DBHelper(this)
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("upload-service", "onStartCommand: $intent ${intent != null}")
        if (intent != null) {
            uploadFor = intent.getStringExtra("uploadFor")
            mediaType = intent.getStringExtra("mediaType")
            mediaUrl = intent.getParcelableExtra<Uri>("mediaUrl")
            audioFileName = intent.getStringExtra("audioFileName")
            senderUid = intent.getStringExtra("senderUid")
            roomId = intent.getStringExtra("roomId")
            name = intent.getStringExtra("name")
            token = intent.getStringExtra("token")
            channelName = intent.getStringExtra("channelName")
            channelImageCaptions = intent.getStringExtra("channelImageCaptions")

            if (mediaType != null && mediaUrl != null) {
                createNotification()
                uploadMedia()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("upload-service", "onBind: ")
        return null
    }

    private fun uploadMedia() {
        Log.d("upload-service", "uploadMedia:mediaType $mediaType")
        Log.d("upload-service", "uploadMedia:url $mediaUrl")
        val reference = storage.reference
            .child("Media")
            .child(Date().time.toString())
        reference.putFile(mediaUrl!!)
            .addOnCompleteListener {
                reference.downloadUrl.addOnCompleteListener { task ->
                    Log.d("upload-service", "uploadData : ${task.result} $task")
                    Log.d("upload-service", "uploadData :uploadFor ${uploadFor}")
                    uploadFor?.let {
                        Log.d("upload-service", "uploadData :uploadFor when : ${it}")
                        when(it){
                            "chat"->{
                                if (senderUid != null && roomId != null)
                                    sendMessage(mediaType!!, task.result.toString())
                            }
                            "status"->{
                                Log.d("upload-service", "uploadMedia:MediaUploadUtils.notifyUploadCompleted(\"status\")")
                                MediaUploadUtils.notifyUploadCompleted("status")
                                addStory(task.result.toString())
                            }
                            "channel"->{
                                addChannelUpdate(task.result.toString())
                            }
                        }

                    }
                }
            }.addOnProgressListener {
                val totalKb = it.totalByteCount / (1024 * 1024)
                val transferredKb = it.bytesTransferred / (1024 * 1024)
                Log.d("upload-service", "uploadMedia: $transferredKb Mb/$totalKb Mb")
                updateNotificationProgress(totalKb, transferredKb)
            }.addOnFailureListener {
                stopSelf()
            }
    }

    private fun sendMessage(mediaType: String, url: String) {
        val fileModel = if (mediaType == "audio") {
            FileModel(url, audioFileName)
        } else {
            FileModel(url, "")
        }
        val messageModel = MessageModel("", mediaType, "", fileModel, senderUid, Date().time)
        val randomKey = database.reference.push().key

        database.reference
            .child("chats")
            .child(roomId!!)
            .child("message")
            .child(randomKey!!)
            .setValue(messageModel)
            .addOnCompleteListener {
                sendPushNotification()
                stopSelf()
            }
            .addOnFailureListener {
                stopSelf()
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendPushNotification() {
        GlobalScope.launch(Dispatchers.IO) {
            val token = token
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()

            val jsonNotif = JSONObject()
            val wholeObj = JSONObject()

            try {
                jsonNotif.put("title", name)
                jsonNotif.put("body", "send you ${this@UploadService.mediaType}")
                wholeObj.put("to", token)
                wholeObj.put("notification", jsonNotif)
            } catch (e: JSONException) {
                Log.d("push-noti", "sendPushNotification: JSONException : ${e.message}")
            }

            val requestBody = RequestBody.create(mediaType, wholeObj.toString())
            val request =
                Request.Builder().url("https://fcm.googleapis.com/fcm/send").post(requestBody)
                    .addHeader("Content-Type", "application/json").addHeader(
                        "Authorization",
                        "key=AAAASjb4r6o:APA91bF-2UtEuSOPjmPsgfAsWMFfg3KRny8Spn0R7CzK2Qmvs39gsW9XTWgNTRrYFS8gqne8mtg-m_OrVszwWpfeI_i_t2ckwpTyM1mpj5Zevouxsq-gTZXSUOD2nY4BitTSXoKk40ff"
                    ).build()

            try {
                val response = client.newCall(request).execute()
                Log.d(
                    "push-noti",
                    "sendPushNotification: response : ${response.body.toString()} ${response.message.toString()} ${response.toString()}"
                )
            } catch (e: IOException) {
                Log.d("push-noti", "sendPushNotification: IOException : ${e.message}")
            }
        }
    }

    private fun createNotification(): Notification {
        Log.d("upload-service-notification", "createNotification() : ")
        val channelId = "upload_progress_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Upload Progress"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Media sending...")
            .setProgress(100, 0, false)
            .build()
    }

    private fun updateNotificationProgress(totalKb: Long, transferredKb: Long) {
        Log.d("upload-service-notification", "updateNotificationProgress() : $transferredKb / $totalKb")
        val channelId = "upload_progress_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Media sending...")
            .setProgress(totalKb.toInt(), transferredKb.toInt(), false)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        if (totalKb == transferredKb) {
            Handler(Looper.getMainLooper()).postDelayed({
                notificationManager.cancel(notificationId)
            },200)
        }
    }

    private fun addStory(url: String) {
        dbHelper.insertUserStory(
            prefs.uid.toString(),
            prefs.userName.toString(),
            prefs.userProfileImage.toString(),
            url,
            getCurrentDateTimeFormatted()
        )
    }

    private fun addChannelUpdate(url: String){
        dbHelper.insertChannelMessage(
            channelName = channelName!!,
            updateType = "image",
            text = channelImageCaptions!!,
            url = url,
        )
    }

    private fun getCurrentDateTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

}