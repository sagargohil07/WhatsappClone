package com.thinkwik.communimate.utils

import android.content.Context
import androidx.startup.Initializer
import com.thinkwik.communimate.di.initKoin

class KoinInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        initKoin(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
