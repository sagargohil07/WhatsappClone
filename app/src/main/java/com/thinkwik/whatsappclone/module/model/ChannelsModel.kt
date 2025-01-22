package com.thinkwik.whatsappclone.module.model

import java.io.Serializable

data class ChannelsModel(
    val channelName: String,
    val channelInfo: String = "",
    val channelImage: String,
    var isFollowing: Int = 0,
    var isMyChannel: Int = 0,
) : Serializable
