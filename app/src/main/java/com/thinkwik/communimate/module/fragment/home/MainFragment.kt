package com.thinkwik.communimate.module.fragment.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentMainBinding
import com.thinkwik.communimate.module.fragment.story.StatusFragment
import com.thinkwik.communimate.prefs.PreferenceStorage
import com.thinkwik.communimate.requireMainActivity
import com.thinkwik.communimate.utils.runOnUiThread
import com.thinkwik.communimate.utils.showDialogPicturePicker
import com.thinkwik.communimate.utils.uriToBitmap
import org.koin.android.ext.android.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainFragment : BaseFragment<FragmentMainBinding>(R.layout.fragment_main) {

    private val prefs: PreferenceStorage by inject()
    private val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    val fragmentList = arrayListOf(
        CommunityFragment(),
        ChatListFragment(),
        StatusFragment(),
        CallsHistoryFragment()
    )

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_IMAGE_GALLARY = 102
    }

    private var photoFile: File? = null
    private var selectedImage: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initListener()
        setUpViewPagerAndTabLayout()
        updateTabs()
    }

    private fun setUpViewPagerAndTabLayout() {
        binding.viewPager.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun getItemCount(): Int = fragmentList.size
            override fun createFragment(position: Int): Fragment = fragmentList[position]
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_group)
                1 -> tab.text = getString(R.string.chats)
                2 -> tab.text = getString(R.string.updates)
                3 -> tab.text = getString(R.string.calls)
            }
        }.attach()

        val layout = (binding.tabLayout.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.5f
        layout.layoutParams = layoutParams

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                runOnUiThread {
                    requireMainActivity().lastVisitedTab = tab.position
                    binding.viewPager.currentItem = tab.position
                    binding.btnSearch.isVisible = !(tab.position == 0 || tab.position == 2)
                    if (tab.position == 0) {
                        tab.icon?.setTint(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.selected_text
                            )
                        )
                    } else {
                        tab.icon?.setTint(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.unselected_text
                            )
                        )
                    }
                    updateTabs()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

    }

    private fun updateTabs() {
        binding.tabLayout.selectTab(
            binding.tabLayout.getTabAt(
                requireMainActivity().lastVisitedTab ?: 1
            )
        )
        when (requireMainActivity().lastVisitedTab) {
            0 -> {
                binding.floatButton.tag = "community"
                binding.mlMain.transitionToStart()
            }

            1 -> {
                binding.floatButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_chat
                    )
                )
                binding.floatButton.tag = "contacts"
                binding.mlMain.transitionToStart()
            }

            2 -> {
                binding.floatButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_camera
                    )
                )
                binding.floatButton.tag = "camera"
                binding.mlMain.transitionToEnd()
            }

            3 -> {
                binding.floatButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_add_call
                    )
                )
                binding.floatButton.tag = "calls"
                binding.mlMain.transitionToStart()
            }

            else -> {
                throw IllegalArgumentException("Unknown Type Tab Seleted")
            }
        }
        if (requireMainActivity().lastVisitedTab == 0) {
            binding.floatButton.visibility = View.GONE
            binding.floatAddStatus.visibility = View.GONE
        } else {
            binding.floatButton.visibility = View.VISIBLE
            binding.floatAddStatus.visibility = View.VISIBLE
        }
    }

    private fun initListener() {
        binding.btnOptions.setOnClickListener {
            Log.d("click", "btnOptions.setOnClickListener: ")
            binding.llMenu.isVisible = !binding.llMenu.isVisible
        }
        binding.btnOptions.setOnClickListener {
            requireMainActivity().showOptionMenu()
        }
        binding.floatAddStatus.setOnClickListener {
            findNavController().navigate(R.id.nav_add_text_story_fragment, null, navOptions)
        }
        binding.btnCamera.setOnClickListener {
            if (checkCameraPermission()) {
                if (binding.floatButton.tag.equals("camera")) {
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
                }
            } else {
                requestCameraPermission()
            }
        }
        binding.floatButton.setOnClickListener {
            if (binding.floatButton.tag == "contacts") {
                findNavController().navigate(MainFragmentDirections.toNavContactFragment())
            } else if (binding.floatButton.tag == "camera") {
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
            }
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
}

