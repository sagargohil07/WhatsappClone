package com.thinkwik.communimate.module.fragment.story

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.thinkwik.communimate.OnMediaUpload
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentStatusBinding
import com.thinkwik.communimate.module.adapter.ChannelsAdapter
import com.thinkwik.communimate.module.adapter.FollowingChannelsAdapter
import com.thinkwik.communimate.module.adapter.StatusAdapter
import com.thinkwik.communimate.module.model.ChannelsModel
import com.thinkwik.communimate.module.model.UserModel
import com.thinkwik.communimate.prefs.PreferenceStorage
import com.thinkwik.communimate.utils.DBHelper
import com.thinkwik.communimate.utils.showDialogPicturePicker
import com.thinkwik.communimate.utils.uriToBitmap
import com.thinkwik.communimate.widget.story.StoryModel
import com.thinkwik.communimate.widget.story.StoryPreference
import org.koin.android.ext.android.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusFragment : BaseFragment<FragmentStatusBinding>(R.layout.fragment_status) {

    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var statusAdapter: StatusAdapter
    private lateinit var viewedStatusAdapter: StatusAdapter
    private lateinit var channelsAdapter: ChannelsAdapter
    private lateinit var followingChannelsAdapter: FollowingChannelsAdapter

    private var storyList: ArrayList<StoryModel> = ArrayList<StoryModel>()
    private var statusList: ArrayList<UserModel> = ArrayList<UserModel>()
    private var myStoryModel: UserModel = UserModel()
    private var otherUsersStoryList: ArrayList<UserModel> = ArrayList<UserModel>()
    private var followingChannelsList: ArrayList<ChannelsModel> = ArrayList<ChannelsModel>()
    private var channelsList: ArrayList<ChannelsModel> = ArrayList<ChannelsModel>()

    private lateinit var dialogBottomSheetDialog: BottomSheetDialog
    private lateinit var dbHelper: DBHelper
    private lateinit var storyPreference: StoryPreference
    private val prefs: PreferenceStorage by inject()

    private var isViewedRvExpanded = false
    private var photoFile: File? = null
    private var selectedImage: Uri? = null

    private val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    companion object {
        const val CAMERA_PERMISSION_CODE = 100
        const val REQUEST_IMAGE_CAPTURE = 101
        const val REQUEST_IMAGE_GALLARY = 102
        const val PENDING_INDICATOR_COLOR = "#009988"
        const val VISITED_INDICATOR_COLOR = "#757C85"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        Log.d("status", "init: ")
        storyPreference = StoryPreference(requireContext())
        dbHelper = DBHelper(requireActivity())
        if (!dbHelper.hasRecordsExceptUid(uid = prefs.uid.toString())) {
            dbHelper.enterOtherUserStory()
        }
        prepareChannelList()
        updateUI()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        initStatusAdapter()
        initViewedStatusAdapter()

        initFollowingChannelsAdapter()
        initChannelsAdapter()

        //getUserStatus()
        initListener()
    }

    private fun initFollowingChannelsAdapter() {
        followingChannelsAdapter =
            FollowingChannelsAdapter(
                requireContext(),
                followingChannelsList
            ) { model ->
                val bundle = Bundle()
                bundle.putSerializable("model", model)
                findNavController().navigate(
                    R.id.nav_show_channel_update_fragment,
                    bundle,
                    navOptions
                )
            }
        binding.rvFollowingChannels.adapter = followingChannelsAdapter
    }

    private fun prepareChannelList(): ArrayList<ChannelsModel> {
        val list = dbHelper.getAllChannels()
        channelsList.clear()
        followingChannelsList.clear()
        Log.d("prepareChannelList", "====================================================")
        list.forEach {
            Log.d("prepareChannelList", "prepareChannelList: ${it.isFollowing} ${it.channelName} ")
            if (it.isFollowing == 0 && it.isMyChannel == 0) {
                channelsList.add(it)
            } else {
                followingChannelsList.add(it)
            }
        }
        Log.d("prepareChannelList", "====================================================")
        return channelsList
    }

    private fun initChannelsAdapter() {
        channelsAdapter =
            ChannelsAdapter(requireContext(), channelsList) { model, isFollowButtonClicked ->
                if (isFollowButtonClicked) {
                    dbHelper.updateChannelIsFollowing(model.channelName, true)
                    val list = prepareChannelList()
                    updateUI()
                    channelsAdapter.updateList(list)
                    followingChannelsAdapter.updateList(followingChannelsList)
                } else {
                    val bundle = Bundle()
                    bundle.putSerializable("model", model)
                    findNavController().navigate(
                        R.id.nav_show_channel_update_fragment,
                        bundle,
                        navOptions
                    )
                }
            }
        binding.rvChannels.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvChannels.adapter = channelsAdapter
    }

    private fun getChannelList(): ArrayList<ChannelsModel> {
        val list = arrayListOf<ChannelsModel>(
            ChannelsModel(
                channelName = "Whatsapp",
                channelInfo = "30M followers",
                channelImage = "https://store-images.s-microsoft.com/image/apps.8985.13655054093851568.1c669dab-3716-40f6-9b59-de7483397c3a.8b1af40f-2a98-4a00-98cd-94e485a04427"
            ),
            ChannelsModel(
                channelName = "kalyani priyadarshan",
                channelInfo = "21.8M followers",
                channelImage = "https://justformoviefreaks.in/wp-content/uploads/2023/02/Best-Movies-of-Kalyani-Priyadarshan.jpg"
            ),
            ChannelsModel(
                channelName = "Allu arjun",
                channelInfo = "13.2M followers",
                channelImage = "https://www.newstap.in/h-upload/2023/07/25/1524656-sakshi-fan-of-aa.webp"
            ),
            ChannelsModel(
                channelName = "Vijay Thalapathy",
                channelInfo = "11.3M followers",
                channelImage = "https://c.ndtvimg.com/2023-09/9jc1kvn8_alia-_625x300_18_September_23.jpg"
            ),
            ChannelsModel(
                channelName = "Nani",
                channelInfo = "10M followers",
                channelImage = "https://www.telugu360.com/wp-content/uploads/2023/09/Nani-1.jpg"
            ),
            ChannelsModel(
                channelName = "Vijay Sethupathi",
                channelInfo = "15M followers",
                channelImage = "https://img.etimg.com/thumb/width-640,height-480,imgsize-56970,resizemode-75,msid-96218309/news/new-updates/vijay-sethupathis-drastic-weight-loss-stuns-fans-see-pics.jpg"
            ),
            ChannelsModel(
                channelName = "Rajinikanth",
                channelInfo = "20M followers",
                channelImage = "https://static.toiimg.com/thumb/msid-93702520,width-1280,resizemode-4/93702520.jpg"
            ),
            ChannelsModel(
                channelName = "Kamal haasan",
                channelInfo = "5M followers",
                channelImage = "https://images.news18.com/ibnlive/uploads/2022/09/kamal-haasan-vikram-1-16623782714x3.jpg"
            ),
            ChannelsModel(
                channelName = "Rashmika mandanna",
                channelInfo = "25M followers",
                channelImage = "https://images.cinemaexpress.com/uploads/user/imagelibrary/2018/9/3/original/Rashmika-Mandanna-.jpg"
            ),
        )
        return list
    }

    private fun updateUI() {
        val allList = dbHelper.getAllUserStories()
        val myStory = getUserStory(prefs.uid)

        myStoryModel = UserModel(
            uid = prefs.uid,
            name = prefs.userName,
            imageUrl = prefs.userProfileImage,
            storyList = myStory
        )
        Log.d("status", "init:allList ${allList.joinToString()}")
        /*
        Log.d("status", "init:myStory ${myStory.joinToString()}")
        */

        val url: String
        if (myStory.isNotEmpty()) {
            binding.llRecent.isVisible = true
            url = myStory[myStory.size - 1].imageUrl.toString()
            binding.ivStatusAdd.visibility = View.GONE
            binding.llAddStatus.background = getIndicatorDrawable(myStory)
        } else {
            url = prefs.userProfileImage.toString()
            /* binding.llRecent.isVisible = false*/
            binding.ivStatusAdd.visibility = View.VISIBLE
            binding.llAddStatus.background = null
        }
        Glide.with(requireActivity())
            .load(url)
            .placeholder(R.drawable.profile)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .error(R.drawable.profile)
            .into(binding.ivStatus)

        if (followingChannelsList.isNotEmpty()) {
            binding.tvChannelInfo.isVisible = false
            binding.btnFindChannels.isVisible = false
            binding.llFollowing.isVisible = true
        } else {
            binding.tvChannelInfo.isVisible = true
            binding.btnFindChannels.isVisible = true
            binding.llFollowing.isVisible = false
        }
    }

    private fun initListener() {
        binding.llMyStatus.setOnClickListener {
            if (myStoryModel.storyList!!.isEmpty()) {
                if (checkCameraPermission()) {
                    requireActivity().showDialogPicturePicker { b: Boolean, s: String ->
                        if (s.equals("camera")) {
                            dispatchTakePictureIntent()
                        } else if (s.equals("gallery")) {
                            val intent = Intent()
                            intent.action = Intent.ACTION_GET_CONTENT
                            intent.type = "image/*"
                            startActivityForResult(intent, REQUEST_IMAGE_GALLARY)
                        }
                    }
                } else {
                    requestCameraPermission()
                }
            } else {
                val bundle = Bundle()
                bundle.putSerializable("userModel", myStoryModel)
                findNavController().navigate(R.id.nav_story_play_fragment, bundle)
            }
        }
        binding.llMyStatus.setOnLongClickListener {
            dbHelper.clearAllUserStories()
            storyPreference.clearStoryPreferences()
            Glide.get(requireContext()).clearMemory()
            Thread {
                Glide.get(requireContext()).clearDiskCache()
            }.start()
            updateUI()
            true
        }
        binding.llViewedExpand.setOnClickListener {
            isViewedRvExpanded = !isViewedRvExpanded
            if (isViewedRvExpanded) {
                binding.ivViewedExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_down
                    )
                )
            } else {
                binding.ivViewedExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_up
                    )
                )
            }
            binding.rvViewedStatus.isVisible = isViewedRvExpanded
        }
        binding.ivChannelAdd.setOnClickListener {
            binding.llChannelOptions.isVisible = !binding.llChannelOptions.isVisible
        }
        binding.rvChannels.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_MOVE -> {
                        rv.parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })
        binding.rvChannels.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_MOVE -> {
                        rv.parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })
        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.nav_channels_fragment)
        }
        binding.btnFindChannels.setOnClickListener {
            findNavController().navigate(R.id.nav_channels_fragment)
        }
        binding.tvCreateChannel.setOnClickListener {
            showBottomSheet()
        }
        binding.tvFindChannels.setOnClickListener {
            binding.llChannelOptions.isVisible = false
            findNavController().navigate(R.id.nav_channels_fragment)
        }
    }

    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_channel, null)
        dialogBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialogBottomSheetDialog.setContentView(dialogView)

        val ivClose = dialogView.findViewById<AppCompatImageView>(R.id.ivClose)
        val btnContinue = dialogView.findViewById<AppCompatButton>(R.id.btnContinue)
        ivClose.setOnClickListener {
            dialogBottomSheetDialog.dismiss()
        }
        btnContinue.setOnClickListener {
            dialogBottomSheetDialog.dismiss()
            findNavController().navigate(R.id.nav_create_channel_fragment)
        }
        if (!dialogBottomSheetDialog.isShowing) {
            binding.llChannelOptions.isVisible = false
            dialogBottomSheetDialog.show()
        }
    }

    private fun initStatusAdapter() {
        statusAdapter =
            StatusAdapter(
                requireContext(),
                storyPreference,
                getOtherUserStory(),
                onItemClick = { model, position ->
                    /* Log.d("story", "initStatusAdapter:onItemClick \n${model.joinToString()} ")*/
                    val bundle = Bundle()
                    val userModel = UserModel(
                        uid = model.uid,
                        name = model.name,
                        imageUrl = model.imageUrl,
                        storyList = model.storyList
                    )
                    bundle.putSerializable("userModel", userModel)
                    findNavController().navigate(R.id.nav_story_play_fragment, bundle)
                })
        binding.rvStatus.adapter = statusAdapter
    }

    private fun initViewedStatusAdapter() {
        viewedStatusAdapter =
            StatusAdapter(
                requireContext(),
                storyPreference,
                statusList,
                onItemClick = { model, position ->

                })
        binding.rvViewedStatus.layoutManager = LinearLayoutManager(requireContext())
        binding.rvViewedStatus.adapter = viewedStatusAdapter
    }

    private fun getUserStory(uid: String?): ArrayList<StoryModel> {
        val list = dbHelper.getAllUserStories()
        val storyList = ArrayList<StoryModel>()
        storyList.clear()
        list.forEach {
            if (it.uid == uid) {
                storyList.add(StoryModel(it.url, it.dateTime))
            }
        }
        return storyList
    }

    private fun getOtherUserStory(): ArrayList<UserModel> {
        val usersIdList = dbHelper.getDistinctUserDetails()
        Log.d("status", "init:usersIdList ${usersIdList.joinToString()}")
        otherUsersStoryList.clear()
        for (item in usersIdList) {
            if (item.uid != prefs.uid)
                otherUsersStoryList.add(
                    UserModel(
                        uid = item.uid,
                        name = item.name,
                        imageUrl = item.userProfile,
                        storyList = getUserStory(item.uid)
                    )
                )
            /*storyList.add(StoryModel(item.url, item.dateTime))*/
        }
        Log.d("status", "init:otherUsersStoryList ${otherUsersStoryList.joinToString()}")
        return otherUsersStoryList
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
            CAMERA_PERMISSION_CODE
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
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (photoFile != null) {
                selectedImage = data?.data
                val imageBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                val bundle = Bundle()
                bundle.putParcelable("imageBitmap", imageBitmap)
                findNavController().navigate(R.id.nav_add_media_story_fragment, bundle)
            }
        }
        if (requestCode == REQUEST_IMAGE_GALLARY && data != null) {
            if (data.data != null) {
                selectedImage = data.data!!
                val imageBitmap = requireActivity().uriToBitmap(requireContext(), selectedImage!!)
                val bundle = Bundle()
                bundle.putParcelable("imageBitmap", imageBitmap)
                findNavController().navigate(R.id.nav_add_media_story_fragment, bundle)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("status", "onResume() : ")
    }

    private fun getIndicatorDrawable(myStory: ArrayList<StoryModel>): Drawable? {
        return if (storyPreference.isStoryVisited(myStory[myStory.size - 1].imageUrl!!)) {
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_visited)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_pending)
        }
    }

}
