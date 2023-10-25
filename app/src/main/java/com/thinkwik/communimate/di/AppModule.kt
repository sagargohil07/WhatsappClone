package com.thinkwik.communimate.di

import android.content.Context
import com.thinkwik.communimate.prefs.AndroidPreferenceStorage
import com.thinkwik.communimate.prefs.PreferenceStorage

import org.koin.dsl.module

val appModule = module {
    fun providePreferenceStorage(context: Context): PreferenceStorage {
        return AndroidPreferenceStorage(context)
    }

    single {
        providePreferenceStorage(get())
    }

}
