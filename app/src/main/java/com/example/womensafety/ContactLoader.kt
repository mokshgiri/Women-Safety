package com.example.womensafety
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import androidx.core.net.toUri
import com.example.womensafety.model.ContactModel

class ContactLoader(private val context: Context) {

//    @SuppressLint("Range")
@SuppressLint("Range")
fun loadContacts(): ArrayList<ContactModel> {
        val contacts = ArrayList<ContactModel>()

        // Define the columns you want to retrieve
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        // Query the contacts using the content resolver
        val contentResolver: ContentResolver = context.contentResolver

        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC" // ASC for ascending order
        )

        // Check if the cursor is not null and contains data
        cursor?.use {
            while (it.moveToNext()) {

                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val photo =  it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                val contact = ContactModel(name, number, photo)
                contacts.add(contact)

                Log.d("ContactLoader", "Name: $name, Number: $number")
            }
        }

        return contacts
    }
}
