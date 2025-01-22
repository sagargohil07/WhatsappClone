package com.thinkwik.whatsappclone.module.fragment.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentUpdateProfileBinding
import com.thinkwik.whatsappclone.module.model.UserModel
import com.thinkwik.whatsappclone.prefs.PreferenceStorage
import com.thinkwik.whatsappclone.requireMainActivity
import com.thinkwik.whatsappclone.utils.runOnUiThread
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream
import java.util.Date

class UpdateProfileFragment :
    BaseFragment<FragmentUpdateProfileBinding>(R.layout.fragment_update_profile) {

    private lateinit var dialog: AlertDialog
    private lateinit var dialogPicture: Dialog
    private lateinit var dialogUserName: BottomSheetDialog

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val prefs: PreferenceStorage by inject()

    private var selectedImage: Uri? = null
    val REQUEST_IMAGE_GALLARY = 1
    val REQUEST_IMAGE_CAPTURE = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initListener()
        updateUi()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        val builder = AlertDialog.Builder(requireContext()).setMessage("Updating Profile")
            .setCancelable(false)
        dialog = builder.create()

        database.reference
            .child("users")
            .child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("profile", "onDataChange: ${snapshot.getValue(UserModel::class.java)}")
                    val userInfo = snapshot.getValue(UserModel::class.java)
                    runOnUiThread {
                        Glide.with(requireActivity())
                            .load(userInfo?.imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(binding.ivProfile)
                        prefs.userName = userInfo?.name
                        updateUi()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun updateUi() {
        binding.tvUserName.text = prefs.userName.toString()
        binding.tvAbout.text = prefs.userName.toString()
        binding.tvNumber.text = prefs.mobileNumber.toString()
    }

    private fun initListener() {
        binding.llUserName.setOnClickListener {
            showBottomSheet()
        }

        binding.ivProfileEdit.setOnClickListener {
            showDialogPicturePicker()
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

    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_profile_name, null)
        dialogUserName = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialogUserName.setContentView(dialogView)

        val etUserName = dialogView.findViewById<AppCompatEditText>(R.id.etUserNameDialog)
        val tvCancel = dialogView.findViewById<AppCompatTextView>(R.id.tvCancelDialog)
        val tvSave = dialogView.findViewById<AppCompatTextView>(R.id.tvSaveDialog)

        val inputMethodManager =
            requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?

        tvCancel.setOnClickListener {
            dialogUserName.dismiss()
        }

        etUserName.setText(binding.tvUserName.text.toString())
        tvSave.setOnClickListener {
            if (etUserName.text.toString().trim().isNotEmpty()) {
                dialogUserName.dismiss()
                updateUserName(etUserName.text.toString().trim())
            }
        }

        if (!dialogUserName.isShowing) {
            inputMethodManager!!.toggleSoftInputFromWindow(
                etUserName.applicationWindowToken,
                InputMethodManager.SHOW_FORCED,
                0
            )
            etUserName.requestFocus()
            dialogUserName.show()
        }
        dialogUserName.setOnDismissListener {
            requireMainActivity().hideKeyboard(binding.tvUserName)
        }
        dialogUserName.setOnCancelListener {
            requireMainActivity().hideKeyboard(binding.tvUserName)
        }

    }

    private fun updateUserName(name: String) {
        val userMap = mapOf("name" to name)
        database.reference.child("users").child(auth.uid.toString()).updateChildren(userMap)
            .addOnCompleteListener {
                Toast.makeText(requireContext(), "profile name updated...", Toast.LENGTH_SHORT)
                    .show()
                Log.d("profile", "updateUserName: ${it.result.toString()}")
                //binding.etUserName.text = it.
                dialog.dismiss()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GALLARY && data != null) {
            if (data.data != null) {
                selectedImage = data.data!!
                dialogPicture.dismiss()
                dialog.show()
                uploadImage()
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
            Log.d("profile", "onActivityResult: ${data.extras?.get("data")}")
            selectedImage = getImageUriFromBitmap(data.extras?.get("data") as Bitmap)
            dialogPicture.dismiss()
            dialog.show()
            uploadImage()
        }
    }

    private fun uploadImage() {
        val reference = storage.reference.child("Profile").child(Date().time.toString())
        reference.putFile(selectedImage!!).addOnCompleteListener {
            reference.downloadUrl.addOnCompleteListener { task ->
                Log.d("otp", "uploadData: ${task.result.toString()} ${task.toString()}")
                val userMap = mapOf("imageUrl" to task.result.toString())
                database.reference.child("users").child(auth.uid.toString()).updateChildren(userMap)
                    .addOnCompleteListener {
                        Toast.makeText(
                            requireContext(),
                            "profile picture updated...",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("profile", "uploadImage: ${it.result.toString()}")
                        prefs.userProfileImage = task.result.toString()
                        dialog.dismiss()
                    }
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