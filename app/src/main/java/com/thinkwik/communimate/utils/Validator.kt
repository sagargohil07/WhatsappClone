package com.thinkwik.communimate.utils

import android.util.Patterns

object Validator {

    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}