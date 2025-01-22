package com.thinkwik.whatsappclone.module.fragment.settings

import android.os.Bundle
import android.view.View
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentAccountBinding

class AccountFragment : BaseFragment<FragmentAccountBinding>(R.layout.fragment_account) {
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