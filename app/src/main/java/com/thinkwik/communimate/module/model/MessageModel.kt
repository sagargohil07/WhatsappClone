package com.thinkwik.communimate.module.model

data class MessageModel(
    var messageKey: String? = "",
    val messageType: String? = "",
    val message: String? = "",
    val file: FileModel? = FileModel(),
    val senderId: String? = "",
    val timeStamp: Long? = 0,
)

data class FileModel(
    val url: String? = "",
    val name: String? = ""
)
