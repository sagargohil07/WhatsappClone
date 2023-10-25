package com.thinkwik.communimate.module.fragment.settings

import android.os.Bundle
import android.view.View
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentHelpBinding


class HelpFragment : BaseFragment<FragmentHelpBinding>(R.layout.fragment_help) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initListener()
    }

    private fun initListener() {

    }
}