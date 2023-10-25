package com.thinkwik.communimate.module.model

import java.io.Serializable

data class CombinedContactsModel(
    val isInDB: Boolean = false,
    val uid: String? = "",
    val name: String? = "",
    val number: String? = "",
    val countryCode: String? = "",
    val countryName: String? = "",
    val imageUrl: String? = "",
    var isContactSelected: Boolean = false
) : Serializable
