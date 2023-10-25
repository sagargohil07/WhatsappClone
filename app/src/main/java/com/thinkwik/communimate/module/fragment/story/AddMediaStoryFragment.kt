package com.thinkwik.communimate.module.fragment.story

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.communimate.MediaType
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentAddMediaStoryBinding
import com.thinkwik.communimate.prefs.PreferenceStorage
import com.thinkwik.communimate.requireMainActivity
import com.thinkwik.communimate.utils.DBHelper
import org.koin.android.ext.android.inject

class AddMediaStoryFragment :
    BaseFragment<FragmentAddMediaStoryBinding>(R.layout.fragment_add_media_story) {

    private var bitmap: Bitmap? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var dialog: AlertDialog

    private lateinit var dbHelper: DBHelper

    private val prefs: PreferenceStorage by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        dbHelper = DBHelper(requireContext())
        val builder = AlertDialog.Builder(requireContext()).setMessage("Uploading your story...")
            .setCancelable(false)
        dialog = builder.create()

        bitmap = arguments?.getParcelable<Bitmap>("imageBitmap")
        if (bitmap != null)
            binding.ivSendImage.setImageBitmap(bitmap)

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
            val bitmap = bitmap
            if (bitmap != null) {
                requireMainActivity().uploadImage(bitmap,MediaType.STATUS)
                findNavController().popBackStack()
            }
        }
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