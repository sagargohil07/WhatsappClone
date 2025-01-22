package com.thinkwik.whatsappclone.module.model

import com.thinkwik.whatsappclone.widget.story.StoryModel
import java.io.Serializable

data class UserModel(
    val uid: String? = "",
    val name: String? = "",
    val number: String? = "",
    val countryCode: String? = "",
    val countryName: String? = "",
    val imageUrl: String? = "",
    val online: String? = "",
    val token: String? = "",
    var storyList: ArrayList<StoryModel>? = ArrayList<StoryModel>()
) : Serializable