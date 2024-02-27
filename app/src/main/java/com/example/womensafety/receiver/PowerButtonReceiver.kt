package com.example.womensafety.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.example.womensafety.firebase.RealtimeDatabaseClass
import com.example.womensafety.model.ContactModel
import com.example.womensafety.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PowerButtonReceiver : BroadcastReceiver() {


    private var helpCount: Int = 0
    private var lastHelpTime: Long = 0
    private lateinit var databaseReference: DatabaseReference

    override fun onReceive(context: Context?, intent: Intent?) {


        if (intent?.action == Intent.ACTION_SCREEN_OFF) {

            // Broadcast the custom action to start speech recognition
            val startSpeechIntent = Intent(ACTION_START_SPEECH_RECOGNITION)
            context?.sendBroadcast(startSpeechIntent)

        }
    }

    private fun loadContactNumbersFromFirebase(context: Context?, message: String, location: String, address: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        databaseReference.child(currentUserUid).child("contactsList").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactNumbers = mutableListOf<String>()

                for (contactSnapshot in snapshot.children) {
                    val contact = contactSnapshot.getValue(ContactModel::class.java)
                    contact?.let {

                        // Assuming you have a property named "contactNumber" in your Contacts class
                        contactNumbers.add(it.number.toString())

                    }
                }

                // Send SOS message to all contact numbers
                sendSms(context, contactNumbers, message, location, address)
                Log.d("contactNumbers", contactNumbers.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    fun processSpeechResults(
        context: Context,
        matches: List<String>,
        location: String,
        address: String
    ) {
        // Check if any of the recognized phrases match the specified keyword ("help")

        Log.d("funCalled", "triggered")
        for (phrase in matches) {
            Log.d("phrase", phrase)
            if (phrase.toLowerCase().contains("help help")) {

                handleHelpPhraseDetected(context, location, address)
                break
            }
        }
    }

    private fun handleHelpPhraseDetected(
        context: Context?,
        location: String,
        address: String
    ) {

        // Trigger SMS alert or perform desired action
        performDesiredAction(context, null.toString(), location, address)
    }

//        }
//        else {
//            // Reset the count if the time difference is greater than the interval
//            helpCount = 1
//        }
//
//        // Update the last help time
//        lastHelpTime = currentTime
//    }

    fun performDesiredAction(
        context: Context?,
        msg: String,
        location: String,
        address: String
    ) {
        // Insert your code here to send an SMS to a specified phone number
//        val phoneNumber = "8368296066" // Replace with the desired phone number
        if (msg == "null") {

            RealtimeDatabaseClass().getCurrentUserMessage {
                val message = it

                loadContactNumbersFromFirebase(context, message, location, address)
            }

//        sendSms(context, phoneNumber, message, location)

        }
        else{
            loadContactNumbersFromFirebase(context, msg, location, address)
        }
    }

    private fun sendSms(
        context: Context?,
        contactNumbers: MutableList<String>,
        message: String,
        location: String,
        address: String
    ) {

        try {
            val smsManager: SmsManager
            if (Build.VERSION.SDK_INT >= 23) {
                //if SDK is greater that or equal to 23 then
                //this is how we will initialize the SmsManager
                smsManager = SmsManager.getDefault()
            } else {
                //if user's SDK is less than 23 then
                //SmsManager will be initialized like this
                smsManager = SmsManager.getDefault()
            }

            for (phoneNumber in contactNumbers) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                smsManager.sendTextMessage(phoneNumber, null, "Location is $location", null, null)
                smsManager.sendTextMessage(phoneNumber, null, "Address is $address", null, null)
                smsManager.sendTextMessage(phoneNumber, null, "#PLAY_SIREN#", null, null)

            }
//            sendMessageToWhatsApp(context!!, contactNumbers, message, location, address)
            // on below line we are sending text message.

            // on below line we are displaying a toast message for message send,
            Toast.makeText(context, "Details have been sent to your added contacts", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {

            // on catch block we are displaying toast message for error.
            Log.d("errorMsg",e.message.toString())
            Toast.makeText(
                context,
                "Please enter all the data.." + e.message.toString(),
                Toast.LENGTH_LONG
            )
                .show()

        }
    }

    fun sendMessageToWhatsApp(
        context: Context,
        location: String,
        address: String
    ) {
        RealtimeDatabaseClass().getCurrentUserMessage {
            val message = it

            val messageWithLocation =
                "$message\n\nMy location is: $location\n\nAddress is: $address"

            // Create a URI for the WhatsApp API
            val uri = Uri.parse(
                "https://api.whatsapp.com/send?phone=" + "&text=" + Uri.encode(messageWithLocation)
            )

            // Create an Intent with ACTION_VIEW and set the data URI to the WhatsApp API URI
            val whatsappIntent = Intent(Intent.ACTION_VIEW, uri)

            // Start the activity
            context.startActivity(whatsappIntent)
        }
    }


    companion object {
        private const val HELP_INTERVAL = 5000 // Time window to count multiple 'help' phrases (milliseconds)
        private const val HELP_COUNT_THRESHOLD = 2 // Number of 'help' phrases to trigger action
        const val ACTION_START_SPEECH_RECOGNITION = "com.example.womensafety.START_SPEECH_RECOGNITION"
        const val SPEECH_REQUEST_CODE = 123
        const val VOLUME_DOWN_BUTTON_PRESS_COUNT = 2
        const val VOLUME_DOWN_BUTTON_INTERVAL = 2000
        const val LOCATION_PERMISSION_REQUEST_CODE = 123

            // ... other constants and methods
    }
}
