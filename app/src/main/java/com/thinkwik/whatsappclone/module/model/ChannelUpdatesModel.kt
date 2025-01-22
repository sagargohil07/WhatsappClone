package com.thinkwik.whatsappclone.module.model

import java.io.Serializable

data class ChannelUpdatesModel(
    val channelName: String,
    val updateType: String ,
    val channelText: String? = "",
    val url: String? = "",
    val dateTime: String? = "",
) : Serializable

