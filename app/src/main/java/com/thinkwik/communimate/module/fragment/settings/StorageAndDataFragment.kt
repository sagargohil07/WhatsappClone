package com.thinkwik.communimate.module.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentStorageAndDataBinding

class StorageAndDataFragment :
    BaseFragment<FragmentStorageAndDataBinding>(R.layout.fragment_storage_and_data) {

    private var isSwitchOn1 = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initListener()
    }

    private fun initListener() {
        binding.ivSwitch1.setOnClickListener {
            isSwitchOn1 = !isSwitchOn1
            val result = if (isSwitchOn1) {
                binding.ivSwitch1.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_100
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_on)
            } else {
                binding.ivSwitch1.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_gray
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_off)
            }
            binding.ivSwitch1.setImageDrawable(result)
        }
    }
}