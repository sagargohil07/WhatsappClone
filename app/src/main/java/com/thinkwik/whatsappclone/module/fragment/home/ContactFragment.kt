package com.thinkwik.whatsappclone.module.fragment.home

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentContactBinding
import com.thinkwik.whatsappclone.module.adapter.ContactsAdapter
import com.thinkwik.whatsappclone.module.model.CombinedContactsModel
import com.thinkwik.whatsappclone.module.model.ContactsModel
import com.thinkwik.whatsappclone.module.model.UserModel
import com.thinkwik.whatsappclone.requireMainActivity
import com.thinkwik.whatsappclone.utils.ContactManager
import com.thinkwik.whatsappclone.utils.runOnUiThread

class ContactFragment : BaseFragment<FragmentContactBinding>(R.layout.fragment_contact) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    var userList: ArrayList<UserModel> = ArrayList<UserModel>()
    private var contactList: ArrayList<ContactsModel> = ArrayList<ContactsModel>()
    private var combinedList: ArrayList<CombinedContactsModel> = ArrayList<CombinedContactsModel>()

    private val CONTACTS_PERMISSION_CODE = 1001
    private val SEND_SMS_PERMISSION_CODE = 101

    private lateinit var contactAdapter: ContactsAdapter
    private val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        getUsersList()
        checkContactPermission()
        initListener()
        initContactsAdapter()
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
            requireMainActivity().hideKeyboard(binding.etSearchContact)
        }

        binding.etSearchContact.addTextChangedListener {
            searchList(it.toString())
        }
    }

    private fun getUsersList() {
        database.reference
            .child("users")
            .addValueEventListener(object : ValueEventListener {
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
                        contactAdapter.updateList(combinedList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun initContactsAdapter() {
        combinedList = combineLists(userList, contactList)
        binding.toolbarInfo.text = "${combinedList.size} contacts"
        Log.d("combineLists", "initContactsAdapter: $combinedList : ${combinedList.joinToString()}")
        binding.tvResultNotFound.isVisible = combinedList.isEmpty()
        contactAdapter = ContactsAdapter(requireContext(), combinedList, { model, position ->
            val userModel = UserModel(
                uid = model.uid,
                name = model.name,
                number = model.number,
                countryCode = model.countryCode,
                countryName = model.countryName,
                imageUrl = model.imageUrl
            )
            val bundle = Bundle()
            bundle.putSerializable("model", userModel)
            findNavController().navigate(R.id.nav_chat_fragment, bundle, navOptions)

        }, {
            if (checkPermission(Manifest.permission.SEND_SMS)) {
                sendSms(it.number.toString(), "connect with me on whatsapp clone now")
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.SEND_SMS),
                    CONTACTS_PERMISSION_CODE
                )
            }
        })
        binding.rvContacts.adapter = contactAdapter
    }

    private fun combineLists(
        userList: ArrayList<UserModel>,
        contactList: ArrayList<ContactsModel>
    ): ArrayList<CombinedContactsModel> {
        val combinedList = mutableListOf<CombinedContactsModel>()
        Log.d("combineLists", "combineLists: userList : ${userList.joinToString()}")
        Log.d("combineLists", "combineLists: contactList : ${contactList.joinToString()}")
        for (contact in contactList) {
            Log.d("combineLists", "==============================================================")

            val cleanedContactNumber = contact.phoneNumber.replace("[()\\s-]".toRegex(), "")
            Log.d("combineLists", "combineLists: cleanedContactNumber : $cleanedContactNumber  <-")

            val matchingUser = userList.find {
                val cleanedUserNumber = it.number?.replace(it.countryCode!!, "")
                Log.d("combineLists", "combineLists: cleanedUserNumber : $cleanedUserNumber")
                cleanedUserNumber == cleanedContactNumber
            }
            Log.d("combineLists", "combineLists: matchingUser : $matchingUser  <-")
            if (matchingUser != null) {
                combinedList.add(
                    CombinedContactsModel(
                        isInDB = true,
                        uid = matchingUser.uid,
                        name = contact.name,
                        number = matchingUser.number,
                        countryCode = matchingUser.countryCode,
                        countryName = matchingUser.countryName,
                        imageUrl = matchingUser.imageUrl
                    )
                )
            } else {
                combinedList.add(
                    CombinedContactsModel(
                        isInDB = false,
                        name = contact.name,
                        number = contact.phoneNumber
                    )
                )
            }
        }
        val sortedCombinedList = combinedList.sortedBy { !it.isInDB }
        return ArrayList(sortedCombinedList)
    }

    private fun searchList(text: String) {
        val filterList = ArrayList<CombinedContactsModel>()
        Log.d("searchList", "searchList:${combinedList.size} text $text ${combinedList.joinToString()}")
        for (i in 0 until combinedList.size) {
            if (combinedList[i].number!!.contains(text, true) || combinedList[i].name!!.contains(
                    text,
                    true
                )
            ) {
                filterList.add(combinedList[i])
            }
        }
        binding.tvResultNotFound.isVisible = filterList.isEmpty()
        contactAdapter.updateList(filterList)
    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, you can read contacts here
            Log.d("contact", "init: Permission is granted")
            readContacts()
            initContactsAdapter()
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
        contactList.clear()
        contactList = ContactManager(requireActivity()).getContacts() as ArrayList<ContactsModel>
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
    }

    private fun sendSms(phoneNumber: String, messageText: String) {
        val smsManager = SmsManager.getDefault()

        // Create a PendingIntent for when the SMS is sent
        val sentIntent = PendingIntent.getBroadcast(
            requireActivity(),
            0,
            Intent("SMS_SENT"),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Create a PendingIntent for when the SMS is delivered
        val deliveredIntent = PendingIntent.getBroadcast(
            requireActivity(),
            0,
            Intent("SMS_DELIVERED"),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Send the SMS
        smsManager.sendTextMessage(
            phoneNumber,
            null,
            messageText,
            sentIntent,
            deliveredIntent
        )

        val smsUri = Uri.parse("smsto:$phoneNumber")
        val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)
        smsIntent.putExtra("sms_body", messageText)
        requireActivity().startActivity(smsIntent)
    }

}