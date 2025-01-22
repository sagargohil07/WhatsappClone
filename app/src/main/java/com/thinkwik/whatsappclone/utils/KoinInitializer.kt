package com.thinkwik.whatsappclone.utils

import android.content.Context
import androidx.startup.Initializer
import com.thinkwik.whatsappclone.di.initKoin

class KoinInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        initKoin(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
