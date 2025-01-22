package com.thinkwik.whatsappclone.extensions

import android.graphics.Color
import android.graphics.Typeface
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.NavController
import com.thinkwik.whatsappclone.utils.DebounceTextWatcher

fun EditText.setDebounce(DELAY: Long = 500, f: () -> Unit) {
    this.addTextChangedListener(object : DebounceTextWatcher(DELAY) {
        override fun onDone() {
            f()
        }
    })
}

fun AppCompatTextView.makeBlueLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {

            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                textPaint.color = Color.parseColor("#31CAFF")
                textPaint.isUnderlineText = false
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        val startIndexOfLink = this.text.toString().indexOf(link.first)
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(
            boldSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}


fun NavController.isFragmentInBackStack(destinationId: Int) = try {
    getBackStackEntry(destinationId)
    true
} catch (e: Exception) {
    false
}

fun AppCompatTextView.makeLinkSelectable(pairText: String, action: (Boolean) -> Unit) {
    this.makeBlueLinks(
        Pair(pairText, View.OnClickListener {
            action(true)
        })
    )
}
