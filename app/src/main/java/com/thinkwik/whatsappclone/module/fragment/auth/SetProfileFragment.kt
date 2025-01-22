package com.thinkwik.whatsappclone.module.fragment.auth

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentSetProfileBinding
import com.thinkwik.whatsappclone.module.model.UserModel
import com.thinkwik.whatsappclone.prefs.PreferenceStorage
import org.koin.android.ext.android.inject
import java.util.Date

class SetProfileFragment : BaseFragment<FragmentSetProfileBinding>(R.layout.fragment_set_profile) {

    private var selectedImage: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    private val prefs: PreferenceStorage by inject()

    private lateinit var dialog: AlertDialog

    private lateinit var phoneNumber: String
    private lateinit var countryCode: String
    private lateinit var countryName: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        phoneNumber = arguments?.getString("phoneNumber").toString()
        countryCode = arguments?.getString("countryCode").toString()
        countryName = arguments?.getString("countryName").toString()

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        val builder = AlertDialog.Builder(requireContext()).setMessage("Creating Profile....")
            .setCancelable(false)
        dialog = builder.create()
        initListener()
    }

    private fun initListener() {
        binding.ivProfile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        binding.btnContinue.setOnClickListener {
            if (selectedImage != null) {
                if (binding.etUserName.text.toString().isNotEmpty()) {
                    dialog.show()
                    uploadData()
                } else {
                    Toast.makeText(requireContext(), "please enter user name", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "please select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadData() {
        val reference = storage.reference.child("Profile").child(Date().time.toString())
        reference.putFile(selectedImage!!).addOnCompleteListener {
            reference.downloadUrl.addOnCompleteListener { task ->
                Log.d("otp", "uploadData: ${task.result.toString()} ${task.toString()}")
                uploadInfo(task.result.toString())
            }
        }
    }

    private fun uploadInfo(imageUrl: String) {
        val user = UserModel(
            auth.uid.toString(),
            binding.etUserName.text.toString(),
            auth.currentUser?.phoneNumber.toString(),
            countryCode,
            countryName,
            imageUrl,
            "true"
        )

        database.reference.child("users")
            .child(auth.uid.toString())
            .setValue(user)
            .addOnCompleteListener {
                prefs.uid = auth.uid.toString()
                prefs.mobileNumber = auth.currentUser?.phoneNumber.toString()
                prefs.isLogin = true
                dialog.dismiss()
                Toast.makeText(requireContext(), "profile created successful", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(SetProfileFragmentDirections.toNavMainFragment())
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (data.data != null) {
                selectedImage = data.data!!
                Glide.with(requireContext()).load(selectedImage).circleCrop()
                    .into(binding.ivProfile)
            }
        }
    }

}