package com.thinkwik.communimate.module.fragment.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentChatBinding
import com.thinkwik.communimate.module.adapter.ChatsAdapter
import com.thinkwik.communimate.module.model.MessageModel
import com.thinkwik.communimate.module.model.UserModel
import com.thinkwik.communimate.requireMainActivity
import com.thinkwik.communimate.services.UploadService
import com.thinkwik.communimate.utils.performBackspaceAction
import com.thinkwik.communimate.utils.runOnUiThread
import com.vanniktech.emoji.Emoji
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener
import com.vanniktech.emoji.listeners.OnEmojiClickListener
import com.vanniktech.ui.hideKeyboard
import com.vanniktech.ui.showKeyboardAndFocus
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Date

class ChatFragment : BaseFragment<FragmentChatBinding>(R.layout.fragment_chat),
    OnEmojiClickListener, OnEmojiBackspaceClickListener {

    lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private var selectedFile: Uri? = null
    private lateinit var roomId: String
    private lateinit var messageList: ArrayList<MessageModel>
    private lateinit var receiverModel: UserModel

    private lateinit var dialogBottomSheet: BottomSheetDialog
    private lateinit var dialogMessageSelectBottomSheet: BottomSheetDialog
    private lateinit var args: UserModel
    private lateinit var chatsAdapter: ChatsAdapter

    companion object {
        const val IMAGE_REQUEST_CODE = 1001
        const val AUDIO_REQUEST_CODE = 1002
        const val VIDEO_REQUEST_CODE = 1003
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args = arguments?.getSerializable("model") as UserModel
        EmojiManager.install(GoogleEmojiProvider())
        binding.emojiView.setUp(
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
                if (binding.llEmojiView.isVisible) {
                    // in here you can do logic when backPress is clicked
                    binding.llEmojiView.isVisible = false
                    binding.btnEmoji.setImageDrawable(requireContext().getDrawable(R.drawable.ic_emoji))
                    requireActivity().hideKeyboard()
                } else {
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
    }

    private fun init() {
        messageList = ArrayList()
        Glide.with(requireContext())
            .load(args.imageUrl)
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop().into(binding.toolbarImage)
        binding.toolbarName.text = args.name
        senderUid = FirebaseAuth.getInstance().uid.toString()
        receiverUid = args.uid.toString()
        roomId = getRoomId(senderUid, receiverUid)
        Log.d("chat", "roomId: ${roomId}")
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        getChats()
        getReceiverData()
        initListener()
        checkPermission()
    }

    private fun getReceiverData() {
        database.goOnline()
        database.reference
            .child("users")
            .child(receiverUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("chat", "single receiver user : ${snapshot.value}")
                    val data = snapshot.getValue(UserModel::class.java)
                    if (data != null) {
                        receiverModel = data
                    }
                    runOnUiThread {
                        setReceiverData()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error $error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setReceiverData() {
        if (receiverModel.online.toBoolean()) {
            binding.toolbarInfo.isVisible = true
            binding.toolbarInfo.text = "online"
        } else {
            binding.toolbarInfo.isVisible = true
            binding.toolbarInfo.text = "offline"
        }
    }

    private fun getChats() {
        database.goOnline()
        database.reference.child("chats").child(roomId).child("message")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("chat", "onDataChange: ${snapshot.children.joinToString()}")
                    messageList.clear()
                    for (item in snapshot.children) {
                        val data = item.getValue(MessageModel::class.java)
                        data?.messageKey = item.key
                        messageList.add(data!!)
                    }
                    chatsAdapter.updateList(messageList)
                    runOnUiThread {
                        binding.rvChats.smoothScrollToPosition(messageList.size)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error $error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun initListener() {
        binding.btnEmoji.setOnClickListener {
            runOnUiThread {
                toggleBottomSheet()
            }
        }
        binding.btnSend.setOnClickListener {
            if (binding.etChat.text.toString().isNotEmpty()) {
                val messageModel = MessageModel(
                    null,
                    "text",
                    binding.etChat.text.toString(),
                    null,
                    senderUid,
                    Date().time
                )
                val randomKey = database.reference.push().key

                database.reference.child("chats").child(roomId).child("message").child(randomKey!!)
                    .setValue(messageModel).addOnCompleteListener {
                        sendNotification(binding.etChat.text.toString())
                        binding.etChat.text!!.clear()
                    }
            }
        }
        binding.btnFile.setOnClickListener {
            showBottomSheet()
        }
        binding.btnSendClose.setOnClickListener {
            clearData()
        }
        binding.ivVoiceCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                requireMainActivity().call(args.uid.toString())
                val bundle = bundleOf(
                    "name" to args.name, "imageUrl" to args.imageUrl
                )
                findNavController().navigate(R.id.nav_outgoing_call_Fragment, bundle)
            }
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

    private fun setupRecyclerView() {
        chatsAdapter = ChatsAdapter(requireContext(), messageList, onVideoPlay = {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(it), "video/*")
            startActivity(intent)
        }, onAudioPlay = { url, isplaying ->

        }, onMessageSelect = {
            showMessageSelectBottomSheet(it)
        })
        binding.rvChats.adapter = chatsAdapter

    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, you can read contacts here
            Log.d("contact", "init: Permission is granted")

        } else {
            // Permission is not granted, request it
            Log.d("contact", "init: Permission is not granted")
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 110
            )
        }
    }

    private fun clearData() {
        binding.llMedia.isVisible = false
        binding.ivSendImage.isVisible = false
        binding.llAudio.isVisible = false
        binding.ivSendImage.setImageDrawable(null)
        selectedFile = null
        binding.tvAudioName.text = null
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
            startActivityForResult(intent, IMAGE_REQUEST_CODE)
        }

        audio.setOnClickListener {
            dialogBottomSheet.dismiss()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "audio/*"
            startActivityForResult(intent, AUDIO_REQUEST_CODE)
        }

        video.setOnClickListener {
            dialogBottomSheet.dismiss()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "video/*"
            startActivityForResult(intent, VIDEO_REQUEST_CODE)
        }

        if (!dialogBottomSheet.isShowing) {
            dialogBottomSheet.show()
        }

    }

    private fun showMessageSelectBottomSheet(messageModel: MessageModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_message_select, null)
        dialogMessageSelectBottomSheet =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialogMessageSelectBottomSheet.setContentView(dialogView)

        val delete = dialogView.findViewById<LinearLayoutCompat>(R.id.llDelete)

        delete.setOnClickListener {
            dialogMessageSelectBottomSheet.dismiss()
            database.reference.child("chats").child(roomId).child("message")
                .child(messageModel.messageKey!!).removeValue()
        }

        if (!dialogMessageSelectBottomSheet.isShowing) {
            dialogMessageSelectBottomSheet.show()
        }

    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(
            "media",
            "onActivityResult: requestCode : $requestCode resultCode : $resultCode data : ${data?.data}"
        )
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (data != null) {
                if (data.data != null) {
                    selectedFile = data.data!!
                    Log.d("media", "onActivityResult: selectedImage $selectedFile")
                    showMedia("image")
                }
            }
        } else if (requestCode == AUDIO_REQUEST_CODE) {
            if (data != null) {
                if (data.data != null) {
                    selectedFile = data.data!!
                    val uri: Uri = data.data!!
                    val uriString = uri.toString()
                    val myFile = File(uriString)
                    val path = myFile.absolutePath
                    var displayName: String? = null
                    if (uriString.startsWith("content://")) {
                        var cursor: Cursor? = null
                        try {
                            cursor =
                                requireActivity().contentResolver.query(uri, null, null, null, null)
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName =
                                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            }
                        } finally {
                            cursor?.close()
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.name
                    }
                    binding.tvAudioName.text = displayName
                    showMedia("audio")
                    Log.d(
                        "media",
                        "onActivityResult: selectedAudio $selectedFile fileName : $displayName"
                    )
                }
            }
        } else if (requestCode == VIDEO_REQUEST_CODE) {
            if (data != null) {
                if (data.data != null) {
                    selectedFile = data.data!!
                    Log.d("media", "onActivityResult: selectedImage $selectedFile ")
                    showMedia("video")
                }
            }
        }
    }

    private fun showMedia(mediaType: String) {
        binding.llMedia.isVisible = true
        if (mediaType == "image") {
            binding.ivSendImage.isVisible = true
            binding.llAudio.isVisible = false
            Glide.with(requireContext()).load(selectedFile).into(binding.ivSendImage)
        } else if (mediaType == "audio") {
            binding.ivSendImage.isVisible = false
            binding.llAudio.isVisible = true
        } else if (mediaType == "video") {
            binding.ivSendImage.isVisible = true
            binding.llAudio.isVisible = false
            Glide.with(requireContext()).load(selectedFile).into(binding.ivSendImage)
        }

        binding.btnSendMedia.setOnClickListener {
            val uploadServiceIntent = Intent(requireActivity(), UploadService::class.java)
            uploadServiceIntent.putExtra("uploadFor", "chat")
            uploadServiceIntent.putExtra("mediaType", mediaType)
            uploadServiceIntent.putExtra("mediaUrl", selectedFile)
            uploadServiceIntent.putExtra("audioFileName", binding.tvAudioName.text.toString())
            uploadServiceIntent.putExtra("senderUid", senderUid)
            uploadServiceIntent.putExtra("roomId", roomId)
            uploadServiceIntent.putExtra("name", args.name)
            uploadServiceIntent.putExtra("token", args.token)
            requireActivity().startService(uploadServiceIntent)
            clearData()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendNotification(body: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val token = args.token
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()

            val jsonNotif = JSONObject()
            val wholeObj = JSONObject()

            try {
                jsonNotif.put("title", args.name)
                jsonNotif.put("body", body)
                wholeObj.put("to", token)
                wholeObj.put("notification", jsonNotif)
            } catch (e: JSONException) {
                Log.d("push-noti", "sendNotification: JSONException : ${e.message}")
            }

            val requestBody = RequestBody.create(mediaType, wholeObj.toString())
            val request =
                Request.Builder().url("https://fcm.googleapis.com/fcm/send").post(requestBody)
                    .addHeader("Content-Type", "application/json").addHeader(
                        "Authorization",
                        "key=AAAASjb4r6o:APA91bF-2UtEuSOPjmPsgfAsWMFfg3KRny8Spn0R7CzK2Qmvs39gsW9XTWgNTRrYFS8gqne8mtg-m_OrVszwWpfeI_i_t2ckwpTyM1mpj5Zevouxsq-gTZXSUOD2nY4BitTSXoKk40ff"
                    ).build()

            try {
                val response = client.newCall(request).execute()
                Log.d(
                    "push-noti",
                    "sendNotification: response : ${response.body.toString()} ${response.message.toString()} ${response.toString()}"
                )
            } catch (e: IOException) {
                Log.d("push-noti", "sendNotification: IOException : ${e.message}")
            }
        }
    }

    private fun getRoomId(senderId: String, receiverId: String): String {
        val asciiValuesStr1 = senderId.filter { it.isLetter() }.map { it.toInt() }
        val asciiValuesStr2 = receiverId.filter { it.isLetter() }.map { it.toInt() }
        val combinedAsciiValues = mutableListOf<Int>()
        val minLength = minOf(asciiValuesStr1.size, asciiValuesStr2.size)
        for (i in 0 until minLength) {
            combinedAsciiValues.add(asciiValuesStr1[i] + asciiValuesStr2[i])
        }
        return combinedAsciiValues.joinToString().replace(",", "").replace(" ", "")
    }

    override fun onEmojiBackspaceClick() {
        if (!binding.llMedia.isVisible) {
            binding.etChat.performBackspaceAction()
            binding.etChat.performBackspaceAction()
        }
    }

    override fun onEmojiClick(emoji: Emoji) {
        if (!binding.llMedia.isVisible) {
            binding.etChat.append(emoji.unicode)
        }
    }

}
