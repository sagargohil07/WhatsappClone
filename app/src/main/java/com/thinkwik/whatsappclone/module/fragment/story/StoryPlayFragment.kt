package com.thinkwik.whatsappclone.module.fragment.story

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentStoryPlayBinding
import com.thinkwik.whatsappclone.module.model.UserModel
import com.thinkwik.whatsappclone.widget.story.StoryModel
import com.thinkwik.whatsappclone.widget.story.StoryPlayerProgressView
import com.thinkwik.whatsappclone.widget.story.StoryPreference

class StoryPlayFragment : BaseFragment<FragmentStoryPlayBinding>(R.layout.fragment_story_play),
    StoryPlayerProgressView.StoryPlayerListener {

    private var stories: ArrayList<StoryModel>? = null
    private var userModel: UserModel = UserModel()
    private val LONG_PRESS_TIMEOUT = 100 // Customize the long press timeout in milliseconds
    private var longPressHandler: Handler? = null
    private var isLongPressHandled = false

    var storyPreference: StoryPreference? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userModel = arguments?.getSerializable("userModel") as UserModel
        stories = userModel.storyList
        init()
    }

    private fun init() {
        Log.d("storyPlay", "init: userModel.imageUrl ${userModel.imageUrl.toString()}")
        Glide.with(requireActivity())
            .load(userModel.imageUrl)
            .placeholder(R.drawable.profile)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .into(binding.toolbarImage)
        binding.toolbarName.text = userModel.name
        storyPreference = StoryPreference(requireActivity())
        binding.progressBarView.setSingleStoryDisplayTime(2000)
        initStoryProgressView()
        initListener()
    }

    private fun initListener() {

    }

    override fun onStartedPlaying(index: Int) {
        loadImage(index)
        //binding.toolbarName.text = stories?.get(index).
        /*name.setText(stories.get(index).name);
        time.setText(stories.get(index).time);
        ;*/
        binding.toolbarInfo.text = stories!![index].dateTime
        stories!![index].imageUrl?.let { storyPreference?.setStoryVisited(it) }
    }

    private fun loadImage(index: Int) {
        binding.progressBarView.pauseProgress()
        Glide.with(this).clear(binding.storyImage)
        Glide.with(this)
            .load(stories!![index].imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBarView.resumeProgress()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBarView.resumeProgress()
                    return false
                }
            }).into(binding.storyImage)
    }

    override fun onFinishedPlaying() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        binding.progressBarView.pauseProgress()
    }

    private fun initStoryProgressView() {
        binding.progressBarView.setStoryPlayerListener(this)
        binding.progressBarView.setProgressBarsCount(stories!!.size)
        setTouchListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener() {
        val gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val imageViewWidth = binding.storyImage.width
                            val touchX = e.x
                            val isLeftSide = touchX < imageViewWidth / 2
                            if (isLeftSide) {
                                binding.progressBarView.previous()
                            } else {
                                binding.progressBarView.next()
                            }
                            true
                        }

                        else -> false
                    }
                    return true
                }
            })

        binding.storyImage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isLongPressHandled = false
                    longPressHandler = Handler()
                    longPressHandler?.postDelayed({
                        if (!isLongPressHandled) {
                            // Handle long press action
                            binding.progressBarView.pauseProgress()
                        }
                    }, LONG_PRESS_TIMEOUT.toLong())
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    longPressHandler?.removeCallbacksAndMessages(null)
                    /*if (!isLongPressHandled) {
                        val imageViewWidth = binding.storyImage.width
                        val touchX = event.x
                        val isLeftSide = touchX < imageViewWidth / 2
                        if (isLeftSide) {
                            binding.progressBarView.previous()
                        } else {
                            binding.progressBarView.next()
                        }
                    }*/
                    binding.progressBarView.resumeProgress()
                }

                MotionEvent.ACTION_MOVE -> {
                    isLongPressHandled = true
                    binding.progressBarView.resumeProgress()
                }
            }

            gestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }

    }

}