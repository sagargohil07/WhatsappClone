package com.thinkwik.whatsappclone.di

import android.content.Context
import com.thinkwik.whatsappclone.prefs.AndroidPreferenceStorage
import com.thinkwik.whatsappclone.prefs.PreferenceStorage

import org.koin.dsl.module

val appModule = module {
    fun providePreferenceStorage(context: Context): PreferenceStorage {
        return AndroidPreferenceStorage(context)
    }

    single {
        providePreferenceStorage(get())
    }

}
