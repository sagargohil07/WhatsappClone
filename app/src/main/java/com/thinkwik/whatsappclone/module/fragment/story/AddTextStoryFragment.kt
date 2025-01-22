package com.thinkwik.whatsappclone.module.fragment.story

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.emoji2.emojipicker.RecentEmojiAsyncProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentAddTextStoryBinding
import com.thinkwik.whatsappclone.prefs.PreferenceStorage
import com.thinkwik.whatsappclone.requireMainActivity
import com.thinkwik.whatsappclone.services.UploadService
import com.thinkwik.whatsappclone.utils.DBHelper
import com.thinkwik.whatsappclone.utils.performBackspaceAction
import com.thinkwik.whatsappclone.utils.runOnUiThread
import com.thinkwik.whatsappclone.utils.setDebounce
import com.vanniktech.emoji.Emoji
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener
import com.vanniktech.emoji.listeners.OnEmojiClickListener
import com.vanniktech.ui.hideKeyboard
import com.vanniktech.ui.showKeyboardAndFocus
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTextStoryFragment :
    BaseFragment<FragmentAddTextStoryBinding>(R.layout.fragment_add_text_story) ,
    OnEmojiClickListener, OnEmojiBackspaceClickListener {

    private var selectedColor = 0
    private lateinit var colorList: List<Int>

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var dialog: AlertDialog
    private lateinit var screenShotImage: Bitmap
    private var selectedImageFile: Uri? = null

    private val prefs: PreferenceStorage by inject()

    private lateinit var dbHelper: DBHelper
    private var fontPos = 0
    private lateinit var fontList: ArrayList<Typeface>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun setColor() {
        selectedColor = colorList.random()
        binding.llRoot.setBackgroundColor(selectedColor)
        binding.llViewContainer.setBackgroundColor(selectedColor)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        dbHelper = DBHelper(requireContext())
        val builder = AlertDialog.Builder(requireContext()).setMessage("Uploading your story...")
            .setCancelable(false)
        dialog = builder.create()
        fontList = arrayListOf(
            resources.getFont(R.font.inter_bold),
            resources.getFont(R.font.autour_one),
            resources.getFont(R.font.kalam),
            resources.getFont(R.font.satisfy),
            resources.getFont(R.font.shadows_into_light),
        )
        colorList = listOf(
            ContextCompat.getColor(requireContext(), R.color.color1),
            ContextCompat.getColor(requireContext(), R.color.color2),
            ContextCompat.getColor(requireContext(), R.color.color3),
            ContextCompat.getColor(requireContext(), R.color.color4),
            ContextCompat.getColor(requireContext(), R.color.color5),
            ContextCompat.getColor(requireContext(), R.color.color6),
            ContextCompat.getColor(requireContext(), R.color.color7),
            ContextCompat.getColor(requireContext(), R.color.color8),
            ContextCompat.getColor(requireContext(), R.color.green_dark_900)
        )
        EmojiManager.install(GoogleEmojiProvider())
        binding.emojiView.setUp(
            rootView = binding.root,
            onEmojiClickListener = this,
            onEmojiBackspaceClickListener = this,
            editText = null,
        )

        setColor()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.etStatus.requestFocus()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etStatus, InputMethodManager.SHOW_IMPLICIT)
        initListener()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.etStatus.setDebounce(100) {
            runOnUiThread {
                binding.btnSend.isVisible = binding.etStatus.text.toString().isNotEmpty()
            }
        }
        binding.btnEmoji.setOnClickListener {
            runOnUiThread {
                toggleBottomSheet()
            }
        }
        binding.btnText.setOnClickListener {
            if (fontPos == fontList.size) {
                fontPos = 0
            }
            val typeface: Typeface = fontList[fontPos]
            binding.etStatus.typeface = typeface
            fontPos++
        }
        binding.btnColor.setOnClickListener {
            setColor()
        }
        binding.btnSend.setOnClickListener {
            binding.etStatus.clearFocus()
            requireMainActivity().hideKeyboard(binding.etStatus)
            Handler(Looper.getMainLooper()).postDelayed({
                screenShotImage = captureScreenshot(binding.llViewContainer)
                selectedImageFile = bitmapToUri(requireMainActivity(),screenShotImage)
                val uploadServiceIntent = Intent(requireActivity(), UploadService::class.java)
                uploadServiceIntent.putExtra("uploadFor", "status")
                uploadServiceIntent.putExtra("mediaType", "image")
                uploadServiceIntent.putExtra("mediaUrl", selectedImageFile)
                requireActivity().startService(uploadServiceIntent)
                findNavController().popBackStack()
                //uploadImage(screenShotImage)
            }, 200)
        }
    }

    private fun toggleBottomSheet() {
        if (binding.llEmojiView.isVisible) {
            binding.llEmojiView.isVisible = false
            binding.btnEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_emoji))
            binding.etStatus.showKeyboardAndFocus()
        } else {
            binding.llEmojiView.isVisible = true
            binding.btnEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_keyboard))
            requireActivity().hideKeyboard()
        }
    }

    private fun captureScreenshot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "screenshot_$timeStamp.jpg"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return Uri.fromFile(file)
    }

    private fun uploadImage(bitmap: Bitmap) {
        dialog.show()
        val reference = storage.reference.child("Story").child("${Date().time}.png")
        val byteArray = convertBitmapToByteArray(bitmap)
        reference.putBytes(byteArray)
            .addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener { task ->
                    Log.d("add-story", "uploadImage: uri :  ${task.toString()}")

                    addStory(task.toString())
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "story upload failed... ${exception.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                dialog.dismiss()
            }
    }

    private fun addStory(url: String) {
        /*val storyModel = StoryModel(url, getCurrentDateTimeFormatted())
        val randomKey = database.reference.push().key
        database.reference
            .child("users")
            .child(prefs.uid.toString())
            .child("storyList")
            .child(randomKey!!)
            .setValue(storyModel).addOnCompleteListener {
                dialog.dismiss()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "story upload failed... ${it.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                dialog.dismiss()
            }*/

        val result = dbHelper.insertUserStory(
            prefs.uid.toString(),
            prefs.userName.toString(),
            prefs.userProfileImage.toString(),
            url,
            getCurrentDateTimeFormatted()
        )
        dialog.dismiss()
        findNavController().popBackStack()
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun getCurrentDateTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onEmojiBackspaceClick() {
        binding.etStatus.performBackspaceAction()
        binding.etStatus.performBackspaceAction()
    }

    override fun onEmojiClick(emoji: Emoji) {
        binding.etStatus.append(emoji.unicode)
    }

}