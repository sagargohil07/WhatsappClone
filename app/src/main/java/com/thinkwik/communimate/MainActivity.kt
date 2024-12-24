package com.thinkwik.communimate

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.thinkwik.communimate.databinding.ActivityMainBinding
import com.thinkwik.communimate.module.model.UserModel
import com.thinkwik.communimate.prefs.PreferenceStorage
import com.thinkwik.communimate.prefs.removeCredentials
import com.thinkwik.communimate.utils.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun Fragment.requireMainActivity() = requireNotNull(activity as? MainActivity) {
    "Fragment $this in not attached to DashboardTabletActivity"
}

enum class UploadFor {
    CHAT,STATUS, CHANNEL_UPDATE
}

interface OnMediaUpload {
    fun onMediaUploaded(url: String)
}

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }
    private val progressingCallTonePlayer: MediaPlayer by lazy {
        MediaPlayer.create(this, R.raw.progress_out).apply { isLooping = true }
    }

    var lastVisitedTab: Int? = 1
    private var isBound: Boolean = false
    private lateinit var storage: FirebaseStorage
    private lateinit var dbHelper: DBHelper
    lateinit var binding: ActivityMainBinding
    var userDetails: UserModel = UserModel()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    var onMediaUploaded: OnMediaUpload? = null

    private val prefs: PreferenceStorage by inject()
    /*    var sinchClient: SinchClient? = null
        var sinchCall: Call? = null
        var sinchCallController: CallController? = null*/
    /*    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SinchService.SinchClientServiceBinder
            sinchClient = binder.sinchClient
            if (sinchClient != null) {
                // Do something with sinchClient
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            sinchClient = null
        }
    }*/

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: ")
        storage = FirebaseStorage.getInstance()
        dbHelper = DBHelper(this)
        setAppTheme()
        setDefaultLanguage()

        val graph = navController.navInflater.inflate(R.navigation.nav_init)
        if (prefs.isLogin) {
            graph.setStartDestination(R.id.nav_main_fragment)
        } else {
            graph.setStartDestination(R.id.nav_login_fragment)
        }
        navController.graph = graph

        init()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        if (prefs.isLogin) {
            getToken()
            checkUser()
        }

        binding.llMenu.setOnClickListener {
            Log.d("click", "llMenu.setOnClickListener: ")
            binding.llMenu.isVisible = false
        }

        binding.btnProfile.setOnClickListener {
            binding.llMenu.isVisible = false
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.nav_update_profile_fragment)
        }

        binding.btnNewGroup.setOnClickListener {
            binding.llMenu.isVisible = false
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val bundle = bundleOf("screen" to "new_group")
            val navOptions = NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right).build()
            navController.navigate(R.id.nav_new_group_fragment, bundle, navOptions)
        }

        binding.btnNewBroadcast.setOnClickListener {
            binding.llMenu.isVisible = false
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val bundle = bundleOf("screen" to "broad_cast")
            val navOptions = NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right).build()
            navController.navigate(R.id.nav_new_group_fragment, bundle, navOptions)
        }

        binding.btnSettings.setOnClickListener {
            binding.llMenu.isVisible = false
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val bundle = bundleOf("user" to userDetails)
            val navOptions = NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right).build()
            navController.navigate(R.id.nav_setting_fragment, bundle, navOptions)
        }

        binding.btnLogout.setOnClickListener {
            binding.llMenu.isVisible = false
            showLogoutDialog()
        }

    }

    fun showOptionMenu() {
        binding.llMenu.isVisible = true
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(this, R.style.DialogStyle)
        dialog.setContentView(R.layout.dialog_logout)
        val d = ColorDrawable(Color.BLACK)
        d.alpha = 150
        dialog.window?.setBackgroundDrawable(d)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        val cancel = dialog.findViewById<AppCompatTextView>(R.id.tvCancel)
        val okay = dialog.findViewById<AppCompatTextView>(R.id.tvOkay)

        okay.setOnClickListener {
            dialog.dismiss()
            prefs.removeCredentials()
            finish()
            startActivity(intent)
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkUser() {
        database.reference.child("users").child(prefs.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        showUnRegisteredAccountDialog()
                    } else {
                        val data = snapshot.getValue(UserModel::class.java)
                        if (data != null) {
                            userDetails = data
                            prefs.token = userDetails.token
                            prefs.userName = userDetails.name
                            prefs.mobileNumber = "${userDetails.number}"
                            prefs.userProfileImage = userDetails.imageUrl
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error :${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun showUnRegisteredAccountDialog() {
        val dialog = Dialog(this, R.style.DialogStyle)
        dialog.setContentView(R.layout.dialog_unregistred_account)
        val d = ColorDrawable(Color.BLACK)
        d.alpha = 150
        dialog.window?.setBackgroundDrawable(d)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        val okay = dialog.findViewById<AppCompatTextView>(R.id.tvOkay)

        okay.setOnClickListener {
            dialog.dismiss()
            prefs.removeCredentials()
            finish()
            startActivity(intent)
        }

        dialog.show()
    }

    private fun updateToken(result: String) {
        if (auth.uid != null && prefs.isLogin) {
            prefs.token = result
            val userMap = mapOf("token" to result)
            database.reference
                .child("users")
                .child(auth.uid.toString())
                .updateChildren(userMap)
                .addOnCompleteListener {
                    /*val intent = Intent(this, SinchService::class.java)
                    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                    handelIncomingCall()*/
                }
        }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                updateToken("")
                return@addOnCompleteListener
            } else {
                updateToken(it.result)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        updateUserStatus("false")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        updateUserStatus("false")
        if (isBound) {
            /*unbindService(serviceConnection)*/
            isBound = false
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        updateUserStatus("true")
    }

    private fun updateUserStatus(isOnline: String) {
        /*if (auth.uid != null && prefs.isLogin) {
                val userMap = mapOf("online" to isOnline)
                database.reference.child("users")
                    .child(auth.uid.toString())
                    .updateChildren(userMap)
                    .addOnCompleteListener {

                    }
            }*/
    }

    private fun handelIncomingCall() {/*sinchClient?.callController?.addCallControllerListener(object : CallControllerListener {
            override fun onIncomingCall(callController: CallController, call: Call) {
                Log.d(TAG, "onIncomingCall: ")
            }
        })*/
    }

    fun call(userId: String) {
        /*sinchCall = sinchClient?.callController?.callUser(userId, MediaConstraints(true))
        sinchCall?.addCallListener(object : CallListener {
            override fun onCallEnded(call: Call) {
                Log.d(TAG, "onCallEnded: ")
                progressingCallTonePlayer.stop()
            }

            override fun onCallEstablished(call: Call) {
                Log.d(TAG, "onCallEstablished: ")
                progressingCallTonePlayer.stop()
            }

            override fun onCallProgressing(call: Call) {
                Log.d(TAG, "onCallProgressing: ")
                progressingCallTonePlayer.start()
            }
        })*/
    }

    fun callEnd() {
        /*sinchCall?.hangup()*/
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun setAppTheme() {
        when (prefs.appTheme.toString().uppercase()) {
            "dark".uppercase() -> {
                // Set dark theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            "light".uppercase() -> {
                // Set light theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            "System default".uppercase() -> {
                // Use the system default mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun setDefaultLanguage() {
        val languageCode = when {
            prefs.appLanguage.equals("English", true) -> {
                "en"
            }

            prefs.appLanguage.equals("Hindi", true) -> {
                "hi"
            }

            prefs.appLanguage.equals("Gujarati", true) -> {
                "gu"
            }

            prefs.appLanguage.equals("Japanese", true) -> {
                "ja"
            }

            prefs.appLanguage.equals("Spanish", true) -> {
                "es"
            }

            prefs.appLanguage.equals("German", true) -> {
                "de"
            }

            prefs.appLanguage.equals("Korean", true) -> {
                "ko"
            }

            /* prefs.appLanguage.equals("French", true) -> {
                 "fr"
             }*/
            else -> {
                "en"
            }
        }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun setLanguage() {
        val languageCode = when {
            prefs.appLanguage.equals("English", true) -> {
                "en"
            }

            prefs.appLanguage.equals("Hindi", true) -> {
                "hi"
            }

            prefs.appLanguage.equals("Gujarati", true) -> {
                "gu"
            }

            prefs.appLanguage.equals("Japanese", true) -> {
                "ja"
            }

            prefs.appLanguage.equals("Spanish", true) -> {
                "es"
            }

            prefs.appLanguage.equals("German", true) -> {
                "de"
            }

            prefs.appLanguage.equals("Korean", true) -> {
                "ko"
            }

            /* prefs.appLanguage.equals("French", true) -> {
                 "fr"
             }*/
            else -> {
                "en"
            }
        }
        changeAppLanguage(languageCode)
    }

    // Change the app's locale based on the selected language
    private fun changeAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        // Refresh the UI to apply the new language
        recreate()
    }

    /*
    fun uploadImage(bitmap: Bitmap, uploadFor: UploadFor) {
        // Use a coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val byteArray = convertBitmapToByteArray(bitmap)
                val reference = storage.reference.child("Story").child("${Date().time}.png")
                // Upload image in the background
                val uploadTask = reference.putBytes(byteArray).await()
                // Get download URL
                val downloadUrl = reference.downloadUrl.await()
                // Process the result on the main thread
                withContext(Dispatchers.Main) {
                    Log.d("add-story", "uploadImage: uri :  $downloadUrl")
                    when (uploadFor) {
                        UploadFor.STATUS -> {
                            addStory(downloadUrl.toString())
                        }

                        UploadFor.CHANNEL_UPDATE -> {
                            onMediaUploaded?.onMediaUploaded(downloadUrl.toString())
                        }

                        else -> {}
                    }
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Story upload failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    *//*dialog.dismiss()*//*
                }
            }
        }
    }

    private suspend fun StorageTask<UploadTask.TaskSnapshot>.await(): UploadTask.TaskSnapshot {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { taskSnapshot ->
                continuation.resume(taskSnapshot)
            }
            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
            addOnProgressListener {
                val totalKb = it.totalByteCount / (1024 * 1024).toFloat() // Total file size in MB
                val transferredKb =
                    it.bytesTransferred / (1024 * 1024).toFloat() // Transferred data in MB

                // Calculate the percentage of data transferred
                val progressPercentage = ((transferredKb / totalKb) * 100).toInt()

                // Use progressPercentage in your notification
                if (totalKb == transferredKb) {
                    showNoty(this@MainActivity, "uploading story", progressPercentage, 100, true)
                } else {
                    showNoty(this@MainActivity, "uploading story", progressPercentage, 100, false)
                }
            }
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun getCurrentDateTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun addStory(url: String) {
        // Use a coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            val result = dbHelper.insertUserStory(
                prefs.uid.toString(),
                prefs.userName.toString(),
                prefs.userProfileImage.toString(),
                url,
                getCurrentDateTimeFormatted()
            )
            onMediaUploaded?.onMediaUploaded(url)
        }
    }

    private fun showNoty(
        context: MainActivity,
        title: String,
        currentProgress: Int,
        maxProgress: Int,
        cancel: Boolean
    ) {
        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("")
            .setContentText("story uploading")
            .setProgress(maxProgress, currentProgress, false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(cancel)

        // Create a notification manager object
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if the device is running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel
            val channel = NotificationChannel(
                "channel_id",
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "story uploading"
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
        // To see the message in logcat
        Log.i("Notify", "$builder")
        // Issue the notification
        notificationManager.notify(1, builder.build())
    }*/



}