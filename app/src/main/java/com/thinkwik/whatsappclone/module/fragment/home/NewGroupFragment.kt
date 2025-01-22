package com.thinkwik.whatsappclone.module.fragment.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentNewGroupBinding
import com.thinkwik.whatsappclone.module.adapter.ContactsAdapter
import com.thinkwik.whatsappclone.module.adapter.SelectedContactsAdapter
import com.thinkwik.whatsappclone.module.model.CombinedContactsModel

class NewGroupFragment : BaseFragment<FragmentNewGroupBinding>(R.layout.fragment_new_group) {

    private lateinit var selectedContactsAdapter: SelectedContactsAdapter
    private lateinit var contactAdapter: ContactsAdapter

    private var selectedContactList: ArrayList<CombinedContactsModel> =
        ArrayList<CombinedContactsModel>()
    private var contactList: ArrayList<CombinedContactsModel> = ArrayList<CombinedContactsModel>()

    private var screen: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screen = arguments?.getString("screen").toString()

        init()
    }

    private fun init() {
        when (screen) {
            "new_group" -> {
                binding.floatButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_right_next
                    )
                )
                binding.rvSelectedContacts.isVisible = true
                binding.llSelectedRv.isVisible = false
            }

            "broad_cast" -> {
                binding.floatButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_done
                    )
                )
                binding.rvSelectedContacts.isInvisible = false
                binding.tvBroadcastInfo.isVisible = true
                binding.toolbarName.text = "New broadcast"
                binding.toolbarInfo.text = "0 of ${contactList.size} selected"
            }
        }
        initListener()
        initContactListAdapter()
        initSelectedContactListAdapter()
    }

    private fun initListener() {
        binding.toolbarSearch.setOnClickListener {
            binding.llSearch.isVisible = true
            binding.etSearchContact.requestFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSearchContact, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.btnBackSearch.setOnClickListener {
            binding.llSearch.isVisible = false
            binding.etSearchContact.setText("")
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearchContact.windowToken, 0)
        }
        binding.etSearchContact.addTextChangedListener {
            searchList(it.toString())
        }
        binding.floatButton.setOnClickListener {
            when (screen) {
                "new_group" -> {
                    val bundle = Bundle()
                    bundle.putSerializable("participantList", selectedContactList)
                    findNavController().navigate(R.id.nav_create_group_fragment, bundle)
                }

                "broad_cast" -> {}
            }
        }
    }

    private fun getContactList(): ArrayList<CombinedContactsModel> {
        contactList.clear()
        contactList.add(
            CombinedContactsModel(
                isInDB = true,
                name = "Sagar",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695968769896?alt=media&token=a7505f86-bdbd-4688-a804-242c2c90b7c1",
                number = null,
            )
        )
        contactList.add(
            CombinedContactsModel(
                isInDB = true,
                name = "Viky",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695185363649?alt=media&token=520982fe-8151-4f78-9302-3b1dc91061a5",
                number = null,
            )
        )
        contactList.add(
            CombinedContactsModel(
                isInDB = true,
                name = "Yash",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694427285333?alt=media&token=911413df-ccd3-4596-8d5d-c35a28f10e13",
                number = null,
            )
        )
        contactList.add(
            CombinedContactsModel(
                isInDB = true,
                name = "Champak",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695962375157?alt=media&token=39170113-1458-42be-97b7-da53e3099eee",
                number = null,
            )
        )
        contactList.add(
            CombinedContactsModel(
                isInDB = true,
                name = "Jetho",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695969518888?alt=media&token=6c3a7888-aedc-441f-aab6-896ebe50aa0f",
                number = null,
            )
        )
        contactList.add(
            CombinedContactsModel(
                isInDB = true,
                name = "Goli",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695185363649?alt=media&token=520982fe-8151-4f78-9302-3b1dc91061a5",
                number = null,
            )
        )

        return contactList
    }

    private fun initContactListAdapter() {
        contactAdapter = ContactsAdapter(requireActivity(),
            getContactList(),
            onContactSelect = { model, position ->
                if (model.isContactSelected) {
                    model.isContactSelected = false
                    contactAdapter.notifyItemChanged(position)

                    selectedContactList.remove(model)
                    selectedContactsAdapter.updateList(selectedContactList)
                } else {
                    model.isContactSelected = true
                    contactAdapter.notifyItemChanged(position)

                    selectedContactList.add(model)
                    selectedContactsAdapter.updateList(selectedContactList)
                }
                updateUI()
            },
            onInvite = { model -> })
        binding.rvContacts.adapter = contactAdapter
    }

    private fun initSelectedContactListAdapter() {
        selectedContactsAdapter =
            SelectedContactsAdapter(requireActivity(), false, selectedContactList) { model ->
                selectedContactList.remove(model)
                contactAdapter.updateItem(model)
                selectedContactsAdapter.updateList(selectedContactList)
                updateUI()
            }
        binding.rvSelectedContacts.adapter = selectedContactsAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        when (screen) {
            "new_group" -> {
                binding.tvBroadcastInfo.isVisible = false
                if (selectedContactList.isNotEmpty()) {
                    binding.llSelectedRv.isVisible = true
                    binding.toolbarInfo.text =
                        "${selectedContactList.size} of ${contactList.size} selected"
                } else {
                    binding.llSelectedRv.isVisible = false
                    binding.toolbarInfo.text = "Add participants"
                }
            }

            "broad_cast" -> {
                if (selectedContactList.isNotEmpty()) {
                    binding.tvBroadcastInfo.isVisible = false
                    binding.rvSelectedContacts.isVisible = true
                    binding.toolbarInfo.text =
                        "${selectedContactList.size} of ${contactList.size} selected"
                } else {
                    binding.tvBroadcastInfo.isVisible = true
                    binding.rvSelectedContacts.isVisible = false
                    binding.toolbarInfo.text = "0 of ${contactList.size} selected"
                }
            }
        }
        binding.etSearchContact.setText("")
    }

    private fun searchList(text: String) {
        val filterList = ArrayList<CombinedContactsModel>()
        for (i in 0 until contactList.size) {
            if (contactList[i].number?.contains(
                    text,
                    true
                ) == true || contactList[i].name!!.contains(
                    text, true
                )
            ) {
                filterList.add(contactList[i])
            }
        }
        contactAdapter.updateList(filterList)
    }


}