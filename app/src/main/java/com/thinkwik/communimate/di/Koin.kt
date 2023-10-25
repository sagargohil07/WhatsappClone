package com.thinkwik.communimate.di

import android.content.Context
import org.koin.android.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

fun initKoin(context: Context) {
    startKoin {
        androidContext(context)
        if (BuildConfig.DEBUG) {
            androidLogger(Level.ERROR)
        }
        modules(
            listOf(
                appModule,
                flavorModule,
                viewModelModule
            )
        )
    }
}
