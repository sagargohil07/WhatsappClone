package com.thinkwik.whatsappclone.module.fragment.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentChatListBinding
import com.thinkwik.whatsappclone.module.adapter.UsersAdapter
import com.thinkwik.whatsappclone.module.model.ContactsModel
import com.thinkwik.whatsappclone.module.model.UserModel
import com.thinkwik.whatsappclone.prefs.PreferenceStorage
import com.thinkwik.whatsappclone.utils.ContactManager
import com.thinkwik.whatsappclone.utils.runOnUiThread
import org.koin.android.ext.android.inject

class ChatListFragment : BaseFragment<FragmentChatListBinding>(R.layout.fragment_chat_list) {

    private val CONTACTS_PERMISSION_CODE = 1001

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    var userList: ArrayList<UserModel> = ArrayList<UserModel>()
    var contactList: ArrayList<ContactsModel> = ArrayList<ContactsModel>()
    var combinedList: ArrayList<UserModel> = ArrayList<UserModel>()
    private val SEND_SMS_PERMISSION_CODE = 101

    private val prefs: PreferenceStorage by inject()

    private lateinit var usersAdapter: UsersAdapter

    private val navOptions =
        NavOptions.Builder().setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right).build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("lifecycle", "onViewCreated: ")
        init()
    }

    private fun init() {
        initListener()
        checkContactPermission()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        initUserAdapter()
        getUsersList()
    }

    private fun getUsersList() {
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (item in snapshot.children) {
                    val user = item.getValue(UserModel::class.java)
                    Log.d("user", "onDataChange: ${user.toString()}")
                    if (user!!.uid != FirebaseAuth.getInstance().uid) {
                        userList.add(user)
                    }
                }
                runOnUiThread {
                    combinedList.clear()
                    combinedList = combineLists(userList, contactList)
                    usersAdapter.updateList(combinedList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun initUserAdapter() {
        combinedList.clear()
        combinedList = combineLists(userList, contactList)
        usersAdapter = UsersAdapter(requireContext(), combinedList) { model, position ->
            val bundle = Bundle()
            bundle.putSerializable("model", model)
            findNavController().navigate(R.id.nav_chat_fragment, bundle, navOptions)
        }
        binding.rvUsers.adapter = usersAdapter
    }

    private fun initListener() {

    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, you can read contacts here
            Log.d("contact", "init: Permission is granted")
            readContacts()
        } else {
            // Permission is not granted, request it
            Log.d("contact", "init: Permission is not granted")
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_CODE
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can read contacts
                Log.d("contact", "onRequestPermissionsResult: is granted")
                readContacts()
            } else {
                // Permission denied, handle accordingly (show a message, etc.)
                Log.d("contact", "onRequestPermissionsResult: not granted")
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    CONTACTS_PERMISSION_CODE
                )
            }
        }
    }

    private fun readContacts() {
        // Code to read contacts goes here
        contactList.clear()
        contactList = ContactManager(requireActivity()).getContacts() as ArrayList<ContactsModel>
        Log.d("contact", "init: contactList |  ${contactList.joinToString()}")
    }

    private fun combineLists(
        userList: ArrayList<UserModel>, contactList: ArrayList<ContactsModel>
    ): ArrayList<UserModel> {
        val combinedList = mutableListOf<UserModel>()
        Log.d("combineLists", "combineLists: userList : ${userList.joinToString()}")
        Log.d("combineLists", "combineLists: contactList : ${contactList.joinToString()}")
        for (contact in contactList) {
            Log.d("combineLists", "==============================================================")

            val cleanedContactNumber = contact.phoneNumber.replace("[()\\s-]".toRegex(), "")
            Log.d("combineLists", "combineLists: cleanedContactNumber : $cleanedContactNumber  <-")

            val matchingUser = userList.find {
                val cleanedUserNumber = it.number?.replace(it.countryCode!!, "")
                cleanedUserNumber == cleanedContactNumber
            }
            Log.d("combineLists", "combineLists: matchingUser : $matchingUser  <-")
            if (matchingUser != null) {
                val isDuplicate = combinedList.any { it.uid == matchingUser.uid }
                if (!isDuplicate) {
                    combinedList.add(
                        UserModel(
                            uid = matchingUser.uid,
                            name = contact.name,
                            number = matchingUser.number,
                            countryCode = matchingUser.countryCode,
                            countryName = matchingUser.countryName,
                            imageUrl = matchingUser.imageUrl,
                            online = matchingUser.online,
                            token = matchingUser.token
                        )
                    )
                }
            }
            Log.d("combineLists", "combineLists: combinedList : ${combinedList.joinToString()}  <-")
        }/* val sortedCombinedList = combinedList.sortedBy { !it.isInDB }*/
        return combinedList as ArrayList<UserModel>
    }

    override fun onDestroyView() {
        Log.d("lifecycle", "onDestroyView: ")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d("lifecycle", "onDestroy: ")/*       database.reference.onDisconnect()
               database.goOffline()*/
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d("lifecycle", "onDetach: ")
        super.onDetach()
    }
}