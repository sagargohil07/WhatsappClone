package com.thinkwik.whatsappclone.module.fragment.story

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentChannelProfileBinding
import com.thinkwik.whatsappclone.module.model.ChannelsModel
import com.thinkwik.whatsappclone.prefs.removeCredentials
import com.thinkwik.whatsappclone.utils.DBHelper

class ChannelProfileFragment :
    BaseFragment<FragmentChannelProfileBinding>(R.layout.fragment_channel_profile) {

    private lateinit var channelModel: ChannelsModel
    private lateinit var dbHelper: DBHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelModel = arguments?.getSerializable("model") as ChannelsModel
        dbHelper = DBHelper(requireContext())
        init()
    }

    private fun init() {
        updateUI()
        initListener()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateUI() {
        if (channelModel.isMyChannel == 1) {
            binding.tvDeleteChannel.isVisible = true

            binding.tvChannelFollow.isVisible = false
            binding.tvUnfollow.isVisible = false
            binding.tvReport.isVisible = false
        }else{
            val icon = if (channelModel.isFollowing == 0) {
                binding.tvUnfollow.isVisible = false
                requireActivity().getDrawable(R.drawable.ic_add)
            } else {
                binding.tvUnfollow.isVisible = true
                requireActivity().getDrawable(R.drawable.ic_done)
            }
            binding.tvChannelFollow.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
        }

        binding.tvChannelName.text = channelModel.channelName
        binding.tvChannelInfo.text = "Channel  ${channelModel.channelInfo}"
        val image = channelModel.channelImage
        Glide.with(requireActivity())
            .load(image)
            .placeholder(R.drawable.profile)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .error(R.drawable.profile)
            .into(binding.ivChannelImage)
    }

    private fun initListener() {
        binding.tvChannelFollow.setOnClickListener {
            channelModel = if (channelModel.isFollowing == 0) {
                dbHelper.updateChannelIsFollowing(channelModel.channelName, true)!!
            } else {
                dbHelper.updateChannelIsFollowing(channelModel.channelName, false)!!
            }
            updateUI()
        }

        binding.tvUnfollow.setOnClickListener {
            channelModel = dbHelper.updateChannelIsFollowing(channelModel.channelName, false)!!
            updateUI()
        }

        binding.tvDeleteChannel.setOnClickListener {
            showDeleteDialog()

        }
    }

    private fun showDeleteDialog() {
        val dialog = Dialog(requireContext(), R.style.DialogStyle)
        dialog.setContentView(R.layout.dialog_logout)
        val d = ColorDrawable(Color.BLACK)
        d.alpha = 150
        dialog.window?.setBackgroundDrawable(d)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        val title = dialog.findViewById<AppCompatTextView>(R.id.tvInfo)
        val cancel = dialog.findViewById<AppCompatTextView>(R.id.tvCancel)
        val okay = dialog.findViewById<AppCompatTextView>(R.id.tvOkay)

        title.text = getString(R.string.are_you_sure_want_to_delete_your_channel)

        okay.setOnClickListener {
            dbHelper.deleteChannel(channelModel.channelName)
            dialog.dismiss()
            findNavController().popBackStack(R.id.nav_main_fragment,false)
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}