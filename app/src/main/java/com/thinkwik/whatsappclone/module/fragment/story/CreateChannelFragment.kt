package com.thinkwik.whatsappclone.module.fragment.story

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentCreateChannelBinding
import com.thinkwik.whatsappclone.utils.DBHelper
import java.io.ByteArrayOutputStream
import java.util.Date

class CreateChannelFragment :
    BaseFragment<FragmentCreateChannelBinding>(R.layout.fragment_create_channel) {

    private var selectedImage: Uri? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var dbHelper: DBHelper
    private lateinit var dialog: AlertDialog
    private lateinit var dialogPicture: Dialog

    private val REQUEST_IMAGE_GALLARY = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())
        storage = FirebaseStorage.getInstance()
        init()
    }

    private fun init() {
        val builder = AlertDialog.Builder(requireContext()).setMessage("Creating Channel....")
            .setCancelable(false)
        dialog = builder.create()
        initListener()
    }

    private fun initListener() {
        binding.ivChannelImage.setOnClickListener {
            showDialogPicturePicker()
        }

        binding.btnCreate.setOnClickListener {
            if (binding.etChannelName.text.toString().isNotEmpty()) {
                if (selectedImage != null) {
                    dialog.show()
                    uploadImage()
                } else {
                    dbHelper.insertChannel(binding.etChannelName.text.toString(), "")
                    findNavController().popBackStack()
                }
            } else {
                Toast.makeText(requireContext(), "provide channel name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDialogPicturePicker() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_picture_type, null)
        dialogPicture = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialogPicture.setContentView(dialogView)

        val gallery = dialogView.findViewById<LinearLayoutCompat>(R.id.llGallery)
        val camera = dialogView.findViewById<LinearLayoutCompat>(R.id.llCamera)

        camera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        gallery.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GALLARY)

        }

        if (!dialogPicture.isShowing) {
            dialogPicture.show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GALLARY && data != null) {
            if (data.data != null) {
                dialogPicture.dismiss()
                selectedImage = data.data!!
                /*
                uploadImage()*/
                Glide.with(requireContext()).load(selectedImage).circleCrop()
                    .into(binding.ivChannelImage)
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
            Log.d("profile", "onActivityResult: ${data.extras?.get("data")}")
            dialogPicture.dismiss()
            selectedImage = getImageUriFromBitmap(data.extras?.get("data") as Bitmap)
            /*uploadImage()*/

            Glide.with(requireContext()).load(selectedImage).circleCrop()
                .into(binding.ivChannelImage)
        }
    }

    private fun uploadImage() {
        val reference = storage.reference
            .child("Profile")
            .child(Date().time.toString())
        reference.putFile(selectedImage!!).addOnCompleteListener {
            reference.downloadUrl.addOnCompleteListener { task ->
                Log.d("otp", "uploadData: ${task.result.toString()} ${task.toString()}")
                dialog.dismiss()
                dbHelper.insertChannel(
                    binding.etChannelName.text.toString(),
                    task.result.toString()
                )
                findNavController().popBackStack()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }

    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            requireActivity().contentResolver, bitmap, "Title", null
        )
        return Uri.parse(path.toString())
    }

}