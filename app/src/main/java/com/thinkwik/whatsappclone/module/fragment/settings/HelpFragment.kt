package com.thinkwik.whatsappclone.module.fragment.settings

import android.os.Bundle
import android.view.View
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentHelpBinding


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