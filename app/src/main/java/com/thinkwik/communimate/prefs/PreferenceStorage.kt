package com.thinkwik.communimate.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface PreferenceStorage {
    var uid: String?
    var userProfileImage: String?
    var token: String?
    var userName: String?
    var emailId: String?
    var mobileNumber: String?
    var isLogin : Boolean
    var appTheme : String?
    var appLanguage : String?
}

enum class Theme{
    SYSTEM_DEFAULT , DARK , LIGHT
}

class AndroidPreferenceStorage(private val context: Context) : PreferenceStorage {

    companion object {
        private const val PREFS_NAME = "Prefs"
        private const val PREFS_ISLOGIN = "ISLOGIN"

        private const val PREFS_TOKEN = "TOKEN"
        private const val PREFS_UID = "UID"
        private const val PREFS_USER_NAME = "USERNAME"
        private const val PREFS_USER_PROFILE_IMAGE = "USERPROFILEIMAGE"
        private const val PREFS_EMAIL_ID = "EMAILID"
        private const val PREFS_MOBILE_NUMBER = "MOBILENUMBER"

        private const val PREFS_APP_THEME = "APPTHEME"
        private const val PREFS_APP_LANGUAGE = "APPLANGUAGE"

        private const val PREFS_USER_STORY_LIST = "USERSTORYLIST"
        private const val DELIMITER = ","

    }

    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override var token: String? by StringPreference(getPrefs(), PREFS_TOKEN,"")
    override var uid: String? by StringPreference(getPrefs(), PREFS_UID,"")
    override var userName: String? by StringPreference(getPrefs(), PREFS_USER_NAME,"")
    override var userProfileImage: String? by StringPreference(getPrefs(), PREFS_USER_PROFILE_IMAGE,"")
    override var emailId: String? by StringPreference(getPrefs(), PREFS_EMAIL_ID,"")
    override var mobileNumber: String? by StringPreference(getPrefs(), PREFS_MOBILE_NUMBER,"")
    override var isLogin: Boolean by BooleanPreference(getPrefs(), PREFS_ISLOGIN, false)
    override var appTheme: String? by StringPreference(getPrefs(), PREFS_APP_THEME,"System default")
    override var appLanguage: String? by StringPreference(getPrefs(), PREFS_APP_LANGUAGE,"English")

}

fun PreferenceStorage.removeCredentials(){
    uid = ""
    token = ""
    isLogin = false
    userName = ""
    emailId = ""
    mobileNumber = ""
}

class StringPreference(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String?
) : ReadWriteProperty<Any, String?> {

    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        preferences.getString(key, defaultValue)
        return preferences.getString(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.edit { putString(key, value) }
    }
}


class BooleanPreference(
    private val preferences: SharedPreferences,
    private val name: String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any, Boolean> {

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return preferences.getBoolean(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        preferences.edit { putBoolean(name, value) }
    }
}