package com.thinkwik.whatsappclone.module.model

import java.io.Serializable

data class StatusModel(
    val uid: String? = "",
    val userName: String? = "",
    val userProfile: String? = "",
    val imageUrl: String? = "",
    val dateTime: String? = "",
) : Serializable
