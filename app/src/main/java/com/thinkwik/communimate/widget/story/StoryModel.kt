package com.thinkwik.communimate.widget.story

import java.io.Serializable

data class StoryModel(
    val imageUrl: String? = "",
    val dateTime: String? = ""
) : Serializable
