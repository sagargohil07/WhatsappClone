package com.thinkwik.whatsappclone.module.fragment.home

import android.os.Bundle
import android.view.View
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentCreateGroupBinding
import com.thinkwik.whatsappclone.module.adapter.SelectedContactsAdapter
import com.thinkwik.whatsappclone.module.model.CombinedContactsModel

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