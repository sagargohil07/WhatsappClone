package com.thinkwik.communimate.utils

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.thinkwik.communimate.module.model.ContactsModel

class ContactManager(private val context: Context) {

    val contact = mutableListOf<ContactsModel>()

    fun getContacts(): List<ContactsModel> {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
        )
        val addedNumbers = HashSet<String>() // To track added phone numbers and prevent duplicates
        val contact = ArrayList<ContactsModel>()

        cursor?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex).toString().replace(" ", "")
                val name = cursor.getString(nameIndex)

                if (!addedNumbers.contains(number)) {
                    // Add the contact only if it's not already added
                    Log.d("cursor", "====================\nname : $name \nnumber : $number")
                    contact.add(ContactsModel(name, number))
                    addedNumbers.add(number) // Add the number to the set to track it
                }
            }
        }

        return contact
    }

}