package com.thinkwik.communimate.utils

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_DEL
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import com.thinkwik.communimate.R
import com.google.android.material.bottomsheet.BottomSheetDialog

fun Fragment?.runOnUiThread(action: () -> Unit) {
    this ?: return
    if (!isAdded) return
    activity?.runOnUiThread(action)
}

fun AppCompatEditText.setDebounce(DELEY: Long = 500, f: () -> Unit) {

    this.addTextChangedListener(object : DebounceTextWatcher(DELEY) {
        override fun onDone() {
            f()
        }
    })
}

inline fun AppCompatEditText.checkLength(
    length: Int = 1,
    crossinline goToNext: (Boolean) -> Unit,
    crossinline goToPrevious: (Boolean) -> Unit,
    crossinline checkValidation: (Boolean) -> Unit
) {
    val et = this

    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            Log.d("et", "afterTextChanged: $s")
            val str = et.text.toString()
            if (str.length > 0 && str.contains(" ")) {
                et.setText(et.getText().toString().replace(" ", ""))
                et.setSelection(et.getText()!!.length)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Log.d("et", "beforeTextChanged: $s")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.d("et", "onTextChanged: $s")
            if (s!!.isEmpty()) {
               /* goToPrevious(true)*/
            } else if (s!!.length == length) {
                goToNext(true)
            }
            checkValidation(true)
        }
    })

    this.setOnKeyListener { v, keyCode, event ->
        Log.d("et", "setOnKeyListener: $event")
        if ( event.action== ACTION_DOWN && keyCode == KEYCODE_DEL && et.text.toString().isEmpty()) {
            goToPrevious(true)
        }
        false
    }
}

fun Activity.showDialogPicturePicker(callBack: (Boolean, String) -> Unit){
    val dialogPicture: Dialog
    val dialogView = layoutInflater.inflate(R.layout.dialog_picture_type, null)
    dialogPicture = BottomSheetDialog(this, R.style.BottomSheetDialog)
    dialogPicture.setContentView(dialogView)

    val gallery = dialogView.findViewById<LinearLayoutCompat>(R.id.llGallery)
    val camera = dialogView.findViewById<LinearLayoutCompat>(R.id.llCamera)

    camera.setOnClickListener {
        /*dispatchTakePictureIntent()*/
        callBack(false,"camera")
        dialogPicture.dismiss()
    }

    gallery.setOnClickListener {
        callBack(false,"gallery")
        dialogPicture.dismiss()

        //val intent = Intent()
        //intent.action = Intent.ACTION_GET_CONTENT
        //intent.type = "image/*"
        //startActivityForResult(intent, REQUEST_IMAGE_GALLARY)

    }

    if (!dialogPicture.isShowing) {
        dialogPicture.show()
    }
}

fun Activity.uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val contentResolver: ContentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun AppCompatEditText.performBackspaceAction() {
    val editable = this.text
    val selectionStart = this.selectionStart
    val selectionEnd = this.selectionEnd

    if (selectionStart > 0) {
        if (selectionStart == selectionEnd) {
            // If no text is selected, delete the character to the left of the cursor
            editable?.delete(selectionStart - 1, selectionStart);
        } else {
            // If text is selected, delete the selected text
            editable?.delete(selectionStart, selectionEnd);
        }
    }
}