package com.thinkwik.whatsappclone.module.fragment.home

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentOutgoingCallBinding
import com.thinkwik.whatsappclone.requireMainActivity

class OutgoingCallFragment :
    BaseFragment<FragmentOutgoingCallBinding>(R.layout.fragment_outgoing_call) {

    private lateinit var name: String
    private lateinit var imageUrl: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        name = arguments?.getString("name").toString()
        imageUrl = arguments?.getString("imageUrl").toString()

        binding.tvName.text = name
        Glide.with(requireContext()).load(imageUrl).circleCrop().into(binding.ivProfile)

        initListener()
    }

    private fun initListener() {
        binding.ivCallEnd.setOnClickListener {
            requireMainActivity().callEnd()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}