package com.thinkwik.communimate.module.fragment.home

import android.os.Bundle
import android.view.View
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentCommunityBinding

class CommunityFragment : BaseFragment<FragmentCommunityBinding>(R.layout.fragment_community) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {

    }

}

