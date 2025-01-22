package com.thinkwik.whatsappclone.widget

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class DragGestureDetector(context: Context, private val onDrag: () -> Unit) : GestureDetector.SimpleOnGestureListener() {
    private val gestureDetector: GestureDetector = GestureDetector(context, this)

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 != null && e2 != null && e2.y - e1.y > FLING_THRESHOLD && Math.abs(velocityY) > VELOCITY_THRESHOLD) {
            // User has dragged down, invoke the onDrag callback
            onDrag()
            return true
        }
        return false
    }

    companion object {
        private const val FLING_THRESHOLD = 200 // Adjust as needed
        private const val VELOCITY_THRESHOLD = 200 // Adjust as needed
    }
}
