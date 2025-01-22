package com.thinkwik.whatsappclone

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.provider.FontRequest
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import com.google.firebase.FirebaseApp
import com.thinkwik.whatsappclone.module.model.UserModel
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider

class MyApp : Application() {

    init {
        instance = this
    }

    companion object {
        lateinit var instance: MyApp
        var context: Context? = null
        lateinit var receiverModel: UserModel
        private var sharedPreferences: SharedPreferences? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
        EmojiManager.install(GoogleEmojiProvider())
    }


}