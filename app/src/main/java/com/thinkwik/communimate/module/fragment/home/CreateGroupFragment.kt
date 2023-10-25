package com.thinkwik.communimate.module.fragment.home

import android.os.Bundle
import android.view.View
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentCreateGroupBinding
import com.thinkwik.communimate.module.adapter.SelectedContactsAdapter
import com.thinkwik.communimate.module.model.CombinedContactsModel

class CreateGroupFragment :
    BaseFragment<FragmentCreateGroupBinding>(R.layout.fragment_create_group) {

    private lateinit var participantAdapter: SelectedContactsAdapter

    private var participantList: ArrayList<CombinedContactsModel> =
        ArrayList<CombinedContactsModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        participantList =
            arguments?.getSerializable("participantList") as ArrayList<CombinedContactsModel>
        init()
    }

    private fun init() {
        binding.tvParticipants.text = "Participants: ${participantList.size}"
        initListener()
        initParticipantAdapter()
    }

    private fun initParticipantAdapter() {
        participantAdapter =
            SelectedContactsAdapter(requireActivity(), true, participantList) { model ->

            }
        binding.rvParticipants.adapter = participantAdapter
    }

    private fun initListener() {

    }

}