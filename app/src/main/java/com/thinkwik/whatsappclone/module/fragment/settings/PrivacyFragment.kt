package com.thinkwik.whatsappclone.module.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentPrivacyBinding


class PrivacyFragment : BaseFragment<FragmentPrivacyBinding>(R.layout.fragment_privacy) {

    private var isSwitchOn = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initListener()
    }

    private fun initListener() {
        binding.ivSwitch.setOnClickListener {
            isSwitchOn = !isSwitchOn
            val result = if (isSwitchOn) {
                binding.ivSwitch.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_100
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_on)
            } else {
                binding.ivSwitch.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_gray
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_off)
            }
            binding.ivSwitch.setImageDrawable(result)
        }
    }

}