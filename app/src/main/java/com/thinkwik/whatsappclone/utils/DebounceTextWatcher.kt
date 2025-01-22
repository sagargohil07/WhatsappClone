package com.thinkwik.whatsappclone.utils

import android.text.Editable
import android.text.TextWatcher
import java.util.Timer
import java.util.TimerTask

abstract class DebounceTextWatcher(val DELAY: Long = 1000): TextWatcher {

    private var timer = Timer()

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        timer.cancel()
        timer = Timer()
        timer.schedule(object : TimerTask(){
            override fun run() {
                onDone()
            }

        },DELAY)
    }

    abstract fun onDone()
}