package com.thinkwik.communimate.module.fragment.home

import android.os.Bundle
import android.view.View
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentCallsHistoryBinding
import com.thinkwik.communimate.module.adapter.CallHistoryAdapter
import com.thinkwik.communimate.module.model.CallHistoryModel

class CallsHistoryFragment :
    BaseFragment<FragmentCallsHistoryBinding>(R.layout.fragment_calls_history) {

    private val callHistoryList: ArrayList<CallHistoryModel> = ArrayList<CallHistoryModel>()
    lateinit var callHistoryAdapter: CallHistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initListener()
        initCallHistotyAdapter()/*getCallHistoryList()*/
    }

    private fun getCallHistoryList(): ArrayList<CallHistoryModel> {
        callHistoryList.clear()
        callHistoryList.add(
            CallHistoryModel(
                userName = "Sagar",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694428107645?alt=media&token=6dd4dea5-06eb-44d0-910d-425b03a963ea",
                callType = "incoming",
                callMediaType = "voice",
                datetime = "yesterday, 2:40 am"
            )
        )
        callHistoryList.add(
            CallHistoryModel(
                userName = "Sagar",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694428107645?alt=media&token=6dd4dea5-06eb-44d0-910d-425b03a963ea",
                callType = "outgoing",
                callMediaType = "voice",
                datetime = "yesterday, 3:40 am"
            )
        )
        callHistoryList.add(
            CallHistoryModel(
                userName = "Viky",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694427285333?alt=media&token=911413df-ccd3-4596-8d5d-c35a28f10e13",
                callType = "incoming",
                callMediaType = "video",
                datetime = "yesterday, 9:35 am"
            )
        )
        callHistoryList.add(
            CallHistoryModel(
                userName = "Viky",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694427285333?alt=media&token=911413df-ccd3-4596-8d5d-c35a28f10e13",
                callType = "outgoing",
                callMediaType = "video",
                datetime = "yesterday, 11:15 am"
            )
        )
        callHistoryList.add(
            CallHistoryModel(
                userName = "Sagar",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694428107645?alt=media&token=6dd4dea5-06eb-44d0-910d-425b03a963ea",
                callType = "incoming",
                callMediaType = "voice",
                datetime = "yesterday, 3:40 am"
            )
        )

        return callHistoryList

    }

    private fun initCallHistotyAdapter() {
        callHistoryAdapter = CallHistoryAdapter(requireContext(), getCallHistoryList())
        binding.rvCallHistory.adapter = callHistoryAdapter
    }

    private fun initListener() {

    }

}