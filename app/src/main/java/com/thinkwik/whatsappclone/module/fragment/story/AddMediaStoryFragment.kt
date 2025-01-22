package com.thinkwik.whatsappclone.module.fragment.story

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentAddMediaStoryBinding
import com.thinkwik.whatsappclone.prefs.PreferenceStorage
import com.thinkwik.whatsappclone.services.UploadService
import com.thinkwik.whatsappclone.utils.DBHelper
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMediaStoryFragment :
    BaseFragment<FragmentAddMediaStoryBinding>(R.layout.fragment_add_media_story) {

    private var bitmap: Bitmap? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var dialog: AlertDialog
    private lateinit var dbHelper: DBHelper
    private val prefs: PreferenceStorage by inject()

    private var selectedImageFile: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())
        bitmap = arguments?.getParcelable<Bitmap>("imageBitmap")
        selectedImageFile = bitmapToUri(requireActivity(), bitmap!!)
        init()
    }

    private fun init() {
        if (bitmap != null) {
            binding.ivSendImage.setImageBitmap(bitmap)
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        initListener()
    }

    private fun initListener() {

        binding.btnSendClose.setOnClickListener {
            binding.ivSendImage.setImageDrawable(null)
            findNavController().popBackStack()
        }

        binding.btnSendMedia.setOnClickListener {
            if (selectedImageFile != null) {
                /*
                requireMainActivity().uploadImage(bitmap, UploadFor.STATUS)
                findNavController().popBackStack()
                */
                val uploadServiceIntent = Intent(requireActivity(), UploadService::class.java)
                uploadServiceIntent.putExtra("uploadFor", "status")
                uploadServiceIntent.putExtra("mediaType", "image")
                uploadServiceIntent.putExtra("mediaUrl", selectedImageFile)
                requireActivity().startService(uploadServiceIntent)
                findNavController().popBackStack()
            }
        }
    }

    private fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
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

    /*    private fun uploadImage(bitmap: Bitmap) {
            dialog.show()

            // Use a coroutine
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val byteArray = convertBitmapToByteArray(bitmap)
                    val reference = storage.reference.child("Story").child("${Date().time}.png")
                    // Upload image in the background
                    val uploadTask = reference.putBytes(byteArray).await()
                    // Get download URL
                    val downloadUrl = reference.downloadUrl.await()
                    // Process the result on the main thread
                    withContext(Dispatchers.Main) {
                        Log.d("add-story", "uploadImage: uri :  $downloadUrl")
                        addStory(downloadUrl.toString())
                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Story upload failed: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                }
            }
        }

        private suspend fun StorageTask<UploadTask.TaskSnapshot>.await(): UploadTask.TaskSnapshot {
            return suspendCancellableCoroutine { continuation ->
                addOnSuccessListener { taskSnapshot ->
                    continuation.resume(taskSnapshot)
                }
                addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            }
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

        private fun addStory(url: String) {
            // Use a coroutine
            lifecycleScope.launch(Dispatchers.IO) {
                val result = dbHelper.insertUserStory(
                    prefs.uid.toString(),
                    prefs.userName.toString(),
                    prefs.userProfileImage.toString(),
                    url,
                    getCurrentDateTimeFormatted()
                )

                // Process the result on the main thread
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    findNavController().popBackStack()
                }
            }
        }*/

}