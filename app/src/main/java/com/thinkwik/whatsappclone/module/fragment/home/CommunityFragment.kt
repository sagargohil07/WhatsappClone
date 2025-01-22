package com.thinkwik.whatsappclone.module.fragment.home

import android.os.Bundle
import android.view.View
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentCommunityBinding

class CommunityFragment : BaseFragment<FragmentCommunityBinding>(R.layout.fragment_community) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {

    }

}

