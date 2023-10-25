package com.thinkwik.communimate.widget.story

import android.content.Context
import android.content.SharedPreferences

class StoryPreference(context: Context) {
    private var preferences: SharedPreferences
    private var editor: SharedPreferences.Editor

    private val PREF_NAME = "storyview_cache_pref"

    init {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = preferences.edit()
    }

    fun clearStoryPreferences() {
        editor.clear()
        editor.apply()
    }

    fun setStoryVisited(uri: String) {
        editor.putBoolean(uri, true)
        editor.apply()
    }

    fun isStoryVisited(uri: String): Boolean {
        return preferences.getBoolean(uri, false)
    }
}
