package com.thinkwik.communimate.module.fragment.story

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.thinkwik.communimate.MediaType
import com.thinkwik.communimate.OnMediaUpload
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentShowChannelUpdateBinding
import com.thinkwik.communimate.module.adapter.ChannelsUpdatesAdapter
import com.thinkwik.communimate.module.model.ChannelUpdatesModel
import com.thinkwik.communimate.module.model.ChannelsModel
import com.thinkwik.communimate.requireMainActivity
import com.thinkwik.communimate.utils.DBHelper
import com.thinkwik.communimate.utils.performBackspaceAction
import com.thinkwik.communimate.utils.runOnUiThread
import com.thinkwik.communimate.utils.showDialogPicturePicker
import com.thinkwik.communimate.utils.uriToBitmap
import com.vanniktech.emoji.Emoji
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener
import com.vanniktech.emoji.listeners.OnEmojiClickListener
import com.vanniktech.ui.hideKeyboard
import com.vanniktech.ui.showKeyboardAndFocus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ShowChannelUpdateFragment :
    BaseFragment<FragmentShowChannelUpdateBinding>(R.layout.fragment_show_channel_update),
    OnEmojiClickListener, OnEmojiBackspaceClickListener , OnMediaUpload {

    private lateinit var channelModel: ChannelsModel
    private lateinit var dbHelper: DBHelper
    private lateinit var channelsUpdatesAdapter: ChannelsUpdatesAdapter
    private var channelUpdateList: ArrayList<ChannelUpdatesModel> = ArrayList<ChannelUpdatesModel>()

    private lateinit var storage: FirebaseStorage
    private lateinit var dialog: AlertDialog

    private lateinit var dialogBottomSheet: BottomSheetDialog
    private var photoFile: File? = null
    private var selectedImage: Uri? = null
    private var bitmap: Bitmap? = null

    private val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelModel = arguments?.getSerializable("model") as ChannelsModel
        dbHelper = DBHelper(requireContext())
        storage = FirebaseStorage.getInstance()
        EmojiManager.install(GoogleEmojiProvider())
        binding.emojiView.setUp(
            rootView = binding.root,
            onEmojiClickListener = this,
            onEmojiBackspaceClickListener = this,
            editText = null,
        )
        binding.emojiView2.setUp(
            rootView = binding.root,
            onEmojiClickListener = this,
            onEmojiBackspaceClickListener = this,
            editText = null,
        )
        init()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(binding.llEmojiView.isVisible){
                    // in here you can do logic when backPress is clicked
                    binding.llEmojiView.isVisible = false
                    binding.btnEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_emoji))
                    requireActivity().hideKeyboard()
                }else if (binding.llEmojiView2.isVisible) {
                    binding.llEmojiView2.isVisible = false
                    binding.btnCaptionEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_emoji))
                    requireActivity().hideKeyboard()
                } else{
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
    }

    private fun init() {
        val builder = AlertDialog.Builder(requireContext())
            .setMessage("Sending Update....")
            .setCancelable(false)
        dialog = builder.create()
        updateUI()
        initUpdateAdapter()
        initListener()
    }

    private fun updateUI() {
        binding.toolbarName.text = channelModel.channelName
        binding.toolbarInfo.text = channelModel.channelInfo
        val image = channelModel.channelImage
        Glide.with(requireActivity())
            .load(image)
            .placeholder(R.drawable.profile)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .error(R.drawable.profile)
            .into(binding.toolbarImage)

        if (channelModel.isMyChannel == 1) {
            binding.llChatAction.isVisible = true
            binding.btnUnfollow.text = getString(R.string.channel_info)
        }

        if (channelModel.isFollowing == 0) {
            binding.btnFollow.isVisible = true
            binding.btnOptions.isVisible = false
        } else {
            binding.btnFollow.isVisible = false
            binding.btnOptions.isVisible = true
        }
        binding.btnFile.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun initListener() {
        binding.btnEmoji.setOnClickListener {
            runOnUiThread {
                toggleBottomSheet()
            }
        }
        binding.btnCaptionEmoji.setOnClickListener {
            runOnUiThread {
                toggleCaptionBottomSheet()
            }
        }
        binding.llChannelProfile.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("model", channelModel)
            findNavController().navigate(
                R.id.nav_channel_profile_fragment,
                bundle,
                navOptions
            )
        }
        binding.llMenu.setOnClickListener {
            Log.d("click", "llMenu.setOnClickListener: ")
            binding.llMenu.isVisible = false
        }
        binding.btnOptions.setOnClickListener {
            binding.llMenu.isVisible = true
        }
        binding.btnFollow.setOnClickListener {
            val result = dbHelper.updateChannelIsFollowing(channelModel.channelName, true)
            Log.d("result", "initListener: ${result}")
            channelModel = result!!
            updateUI()
        }
        binding.btnUnfollow.setOnClickListener {
            binding.llMenu.isVisible = false
            if (channelModel.isMyChannel == 1) {
                val bundle = Bundle()
                bundle.putSerializable("model", channelModel)
                findNavController().navigate(R.id.nav_channel_profile_fragment, bundle, navOptions)
            } else {
                val result = dbHelper.updateChannelIsFollowing(channelModel.channelName, false)
                Log.d("result", "initListener: ${result}")
                channelModel = result!!
                updateUI()
            }
        }
        binding.btnSend.setOnClickListener {
            if (binding.etChat.text.toString().isNotEmpty()) {
                dbHelper.insertChannelMessage(
                    channelName = channelModel.channelName,
                    updateType = "text",
                    text = binding.etChat.text.toString().trim()
                )
                getNewUpdate()
            }
        }
        binding.btnFile.setOnClickListener {
            if (checkCameraPermission()) {
                requireActivity().showDialogPicturePicker { b: Boolean, s: String ->
                    if (s.equals("camera")) {
                        dispatchTakePictureIntent()
                    } else if (s.equals("gallery")) {
                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "image/*"
                        startActivityForResult(intent, StatusFragment.REQUEST_IMAGE_GALLARY)
                    }
                }
            } else {
                requestCameraPermission()
            }
        }
        binding.btnSendMedia.setOnClickListener {
            uploadImage(bitmap!!)
            requireMainActivity().uploadImage(bitmap!!,MediaType.CHANNEL_UPDATE)
        }
        binding.btnSendClose.setOnClickListener {
            selectedImage = null
            binding.llSendMedia.isVisible = false
        }
    }

    private fun toggleBottomSheet() {
        if (binding.llEmojiView.isVisible) {
            binding.llEmojiView.isVisible = false
            binding.btnEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_emoji))
            binding.etChat.showKeyboardAndFocus()
        } else {
            binding.llEmojiView.isVisible = true
            binding.btnEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_keyboard))
            requireActivity().hideKeyboard()
        }
    }

    private fun toggleCaptionBottomSheet() {
        if (binding.llEmojiView2.isVisible) {
            binding.llEmojiView2.isVisible = false
            binding.btnCaptionEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_emoji))
            binding.etCaption.showKeyboardAndFocus()
        } else {
            binding.llEmojiView2.isVisible = true
            binding.btnCaptionEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_keyboard))
            requireActivity().hideKeyboard()
        }
    }

    private fun getNewUpdate() {
        binding.etChat.setText("")
        binding.etCaption.setText("")
        Handler(Looper.getMainLooper()).postDelayed({
            channelUpdateList = getChannelUpdates()
            channelsUpdatesAdapter.updateList(channelUpdateList)
            binding.rvChannelUpdates.smoothScrollToPosition(channelUpdateList.size)
        }, 1000)
    }

    private fun initUpdateAdapter() {
        channelUpdateList.clear()
        channelUpdateList = getChannelUpdates()
        channelsUpdatesAdapter = ChannelsUpdatesAdapter(requireContext(), channelUpdateList)
        binding.rvChannelUpdates.adapter = channelsUpdatesAdapter
        binding.rvChannelUpdates.smoothScrollToPosition(channelUpdateList.size)
    }

    private fun getChannelUpdates(): ArrayList<ChannelUpdatesModel> {
        val list = dbHelper.getAllChannelUpdates()
        return list as ArrayList<ChannelUpdatesModel>
    }

    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_media_type, null)
        dialogBottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialogBottomSheet.setContentView(dialogView)

        val image = dialogView.findViewById<LinearLayoutCompat>(R.id.llImages)
        val audio = dialogView.findViewById<LinearLayoutCompat>(R.id.llAudio)
        val video = dialogView.findViewById<LinearLayoutCompat>(R.id.llVideo)

        image.setOnClickListener {
            dialogBottomSheet.dismiss()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        audio.setOnClickListener {
            dialogBottomSheet.dismiss()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "audio/*"
            startActivityForResult(intent, 2)
        }

        video.setOnClickListener {
            dialogBottomSheet.dismiss()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "video/*"
            startActivityForResult(intent, 3)
        }

        if (!dialogBottomSheet.isShowing) {
            dialogBottomSheet.show()
        }

    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            StatusFragment.CAMERA_PERMISSION_CODE
        )
    }

    private fun dispatchTakePictureIntent() {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        photoFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )

        // Save the photoFile URI for the camera to save the image
        val photoURI: Uri = FileProvider.getUriForFile(
            requireContext(),
            "com.thinkwik.communimate.fileprovider",
            photoFile!!
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(cameraIntent, StatusFragment.REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == StatusFragment.CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == StatusFragment.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (photoFile != null) {
                val imageBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                bitmap = imageBitmap
                binding.ivSendImage.setImageBitmap(bitmap)
                binding.llSendMedia.isVisible = true
            }
        }
        if (requestCode == StatusFragment.REQUEST_IMAGE_GALLARY && data != null) {
            if (data.data != null) {
                selectedImage = data.data!!
                val imageBitmap = requireActivity().uriToBitmap(requireContext(), selectedImage!!)
                bitmap = imageBitmap
                binding.ivSendImage.setImageBitmap(bitmap)
                binding.llSendMedia.isVisible = true
            }
        }
    }

    private fun uploadImage(bitmap: Bitmap) {
        dialog.show()
        // Use a coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val byteArray = convertBitmapToByteArray(bitmap)
                val reference = storage.reference.child("Story").child("${Date().time}.png")
                // Upload image in the background
                val uploadTask = reference.putBytes(byteArray).await()
                val downloadUrl = reference.downloadUrl.await()
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    Log.d("add-story", "uploadImage: uri :  $downloadUrl")
                    dbHelper.insertChannelMessage(
                        channelName = channelModel.channelName,
                        updateType = "image",
                        text = binding.etCaption.text.toString().trim(),
                        url = downloadUrl.toString(),
                    )
                    selectedImage = null
                    binding.llSendMedia.isVisible = false

                    getNewUpdate()
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

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
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

    override fun onEmojiBackspaceClick() {
        if (binding.llSendMedia.isVisible) {
            binding.etCaption.performBackspaceAction()
            binding.etCaption.performBackspaceAction()

        } else {
            binding.etChat.performBackspaceAction()
            binding.etChat.performBackspaceAction()

        }
    }

    override fun onEmojiClick(emoji: Emoji) {
        if (binding.llSendMedia.isVisible) {
            binding.etCaption.append(emoji.unicode)
        } else {
            binding.etChat.append(emoji.unicode)
        }
    }

    override fun onMediaUploaded(url: String) {
        dbHelper.insertChannelMessage(
            channelName = channelModel.channelName,
            updateType = "image",
            text = binding.etCaption.text.toString().trim(),
            url = url,
        )
        selectedImage = null
        binding.llSendMedia.isVisible = false

        getNewUpdate()
    }

}