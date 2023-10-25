package com.thinkwik.communimate.services

import android.app.ActivityManager
import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.sinch.rtc.vvc.reference.app.utils.jwt.JWTFetcher
import com.thinkwik.communimate.R
import com.thinkwik.communimate.jwt.FakeJWTFetcher
import com.thinkwik.communimate.prefs.PreferenceStorage
import org.koin.android.ext.android.inject
import java.io.File

class SinchService : Service() {

    companion object {
        private const val APP_KEY = "aa49b051-b43c-4e6f-90ea-481ec8245068"
        private const val APP_SECRET = "2UJXBlBy30SNqrk/omlYLw=="
        private const val ENVIRONMENT = "ocra.api.sinch.com"

        const val NOTIFICATION_CHANNEL_ID = "SinchClientServiceChannel"
        const val NOTIFICATION_ID = 25
        const val NOTIFICATION_PENDING_INTENT_ID = 1212
    }

    private val notificationSoundUri: Uri by lazy {
        Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE
                    + File.pathSeparator + File.separator + File.separator
                    + packageName
                    + File.separator
                    + R.raw.progress_tone
        )
    }
    private val prefs: PreferenceStorage by inject()
    private val jwtFetcher: JWTFetcher by lazy {
        FakeJWTFetcher()
    }

    /*
        private var sinchClientInstance: SinchClient? = null
    */
    private val isInForeground: Boolean get() = checkIfInForeground()

    override fun onCreate() {
        super.onCreate()
        registerSinchClientIfNecessary()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return SinchClientServiceBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    override fun onDestroy() {
        /*if (sinchClientInstance != null && sinchClientInstance?.isStarted == true) {
            sinchClientInstance?.terminateGracefully()
        }*/
        super.onDestroy()
    }

    private fun registerSinchClientIfNecessary() {
        /*if ((sinchClientInstance != null && sinchClientInstance?.isStarted == true && prefs.isLogin && prefs.token.isNullOrEmpty())) {
            return
        }
        Log.d("", "Regsitering sinch client for user ${prefs.uid}")
        sinchClientInstance = SinchClient.builder()
            .context(this)
            .environmentHost(ENVIRONMENT)
            .userId(prefs.uid.toString())
            .applicationKey(APP_KEY)
            .pushConfiguration(
                PushConfiguration.fcmPushConfigurationBuilder()
                    .senderID(FirebaseApp.getInstance().options.gcmSenderId.orEmpty())
                    .registrationToken(prefs.token.toString())
                    .build()
            )
            .build()
            .apply {
                addSinchClientListener(object : SinchClientListener {
                    override fun onClientFailed(client: SinchClient, error: SinchError) {
                        Log.d("since-service", "onClientFailed: ")

                    }

                    override fun onClientStarted(client: SinchClient) {
                        Log.d("since-service", "onClientStarted: ")

                    }

                    override fun onCredentialsRequired(clientRegistration: ClientRegistration) {
                        Log.d("since-service", "onCredentialsRequired: ")
                        jwtFetcher.acquireJWT(APP_KEY, prefs.uid.toString()) { jwt ->
                            clientRegistration.register(jwt)
                        }
                    }

                    override fun onLogMessage(level: Int, area: String, message: String) {
                        Log.d("since-service", "onLogMessage: ${message}")
                    }

                    override fun onPushTokenRegistered() {
                        Log.d("since-service", "onPushTokenRegistered: ")
                    }

                    override fun onPushTokenRegistrationFailed(error: SinchError) {
                        Log.d("since-service", "onPushTokenRegistrationFailed: ${error.message}")
                    }

                    override fun onPushTokenUnregistered() {
                        Log.d("since-service", "onPushTokenUnregistered: ")
                    }

                    override fun onPushTokenUnregistrationFailed(error: SinchError) {
                        Log.d("since-service", "onPushTokenUnregistrationFailed: ${error.message}")
                    }

                    override fun onUserRegistered() {
                        Log.d("since-service", "onUserRegistered: ")
                    }

                    override fun onUserRegistrationFailed(error: SinchError) {
                        Log.d("since-service", "onUserRegistrationFailed: ${error.message}")
                    }
                })
                callController.addCallControllerListener(object :
                    CallControllerListener {
                    override fun onIncomingCall(callController: CallController, call: Call) {
                        Log.d("since-service", "onIncomingCall: ")
                        call.addCallListener(object : CallListener {
                            override fun onCallEnded(call: Call) {

                            }

                            override fun onCallEstablished(call: Call) {

                            }

                            override fun onCallProgressing(call: Call) {

                            }
                        })
                    }
                })
                start()
            }*/
    }

    private fun createClient() {
        /*if ((sinchClientInstance != null && sinchClientInstance?.isStarted == true)) {
            return
        }
        sinchClientInstance = SinchClient.builder().context(applicationContext)
            .userId(prefs.userName.toString())
            .applicationKey(APP_KEY)
            .environmentHost(ENVIRONMENT)
            .pushConfiguration(
                PushConfiguration.fcmPushConfigurationBuilder()
                    .senderID(FirebaseApp.getInstance().options.gcmSenderId.orEmpty())
                    .registrationToken(prefs.token.toString()).build()
            )
            .pushNotificationDisplayName("User ${prefs.userName.toString()}")
            .build()
        sinchClientInstance?.addSinchClientListener(object : SinchClientListener {
            override fun onClientFailed(client: SinchClient, error: SinchError) {
                Log.d("since-service", "onClientFailed: ")

            }

            override fun onClientStarted(client: SinchClient) {
                Log.d("since-service", "onClientStarted: ")

            }

            override fun onCredentialsRequired(clientRegistration: ClientRegistration) {
                Log.d("since-service", "onCredentialsRequired: ")
                jwtFetcher.acquireJWT(APP_KEY, prefs.uid.toString()) { jwt ->
                    clientRegistration.register(jwt)
                }
            }

            override fun onLogMessage(level: Int, area: String, message: String) {
                Log.d("since-service", "onLogMessage: ${message}")
            }

            override fun onPushTokenRegistered() {
                Log.d("since-service", "onPushTokenRegistered: ")
            }

            override fun onPushTokenRegistrationFailed(error: SinchError) {
                Log.d("since-service", "onPushTokenRegistrationFailed: ${error.message}")
            }

            override fun onPushTokenUnregistered() {
                Log.d("since-service", "onPushTokenUnregistered: ")
            }

            override fun onPushTokenUnregistrationFailed(error: SinchError) {
                Log.d("since-service", "onPushTokenUnregistrationFailed: ${error.message}")
            }

            override fun onUserRegistered() {
                Log.d("since-service", "onUserRegistered: ")
            }

            override fun onUserRegistrationFailed(error: SinchError) {
                Log.d("since-service", "onUserRegistrationFailed: ${error.message}")
            }
        })
        sinchClientInstance?.callController?.addCallControllerListener(object :
            CallControllerListener {
            override fun onIncomingCall(callController: CallController, call: Call) {

            }
        })
        sinchClientInstance!!.start()*/
    }

    inner class SinchClientServiceBinder : Binder() {
        /* val sinchClient: SinchClient? get() = sinchClientInstance*/
    }

    /*fun getSinchClient(): SinchClient? {
        return sinchClientInstance
    }*/

    private fun checkIfInForeground(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val appProcesses: List<ActivityManager.RunningAppProcessInfo> =
            activityManager.runningAppProcesses ?: return false
        return appProcesses.any { appProcess: ActivityManager.RunningAppProcessInfo ->
            appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName
        }
    }

}