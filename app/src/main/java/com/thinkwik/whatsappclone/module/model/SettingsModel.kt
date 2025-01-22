package com.thinkwik.whatsappclone.module.model

import android.graphics.drawable.Drawable

data class SettingsModel(
    val screen: String = "",
    val icon: Drawable? = null,
    val title: String = "",
    val info: String = "",
)
