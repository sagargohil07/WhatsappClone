package com.thinkwik.communimate.utils

import android.util.Log

object MediaUploadUtils {
    private var callback: MediaUploadCallBack? = null

    fun registerCallback(callback: MediaUploadCallBack) {
        Log.d("upload-service", "uploadMedia:::MediaUploadUtils:::registerCallback:::")
        this.callback = callback
    }

    fun notifyUploadCompleted(uploadFor :String) {
        Log.d("upload-service", "uploadMedia:::MediaUploadUtils:::notifyUploadCompleted :::")
        callback?.onUploadComplete(uploadFor =uploadFor )
    }
}