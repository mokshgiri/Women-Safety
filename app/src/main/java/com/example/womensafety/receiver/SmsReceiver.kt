package com.example.womensafety.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.womensafety.R

class SmsReceiver : BroadcastReceiver() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("workingReceiver", "trigerred")

        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {

            mediaPlayer = MediaPlayer.create(context, R.raw.alert_siren)
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>
                val messages = arrayOfNulls<SmsMessage>(pdus.size)

                for (i in pdus.indices) {
                    messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)

                    Log.d("msgs", messages[i].toString())
                }

                // Process the received SMS
                processReceivedSms(context, messages)
            }
        }
    }

    private fun processReceivedSms(context: Context?, messages: Array<SmsMessage?>) {
        for (message in messages) {
            val sender = message?.originatingAddress
            val messageBody = message?.messageBody

            // Handle the received SMS content
            Log.d("SmsReceiver", "Sender: $sender, Message: $messageBody")

            // Pass the received SMS to your desired logic
            // (e.g., check for special commands, trigger actions, etc.)
            // You can pass this information to your existing logic for SOS handling.
            handleReceivedSms(context, sender, messageBody)
        }
    }

    private fun handleReceivedSms(context: Context?, sender: String?, messageBody: String?) {
        // Implement your logic to handle the received SMS
        // This is where you can check for special commands, trigger actions, etc.
        // For example, check if the message contains the play siren command.
        Log.d("smss", messageBody.toString())
        if (messageBody?.contains("#PLAY_SIREN#") == true) {
            // Trigger the siren on the recipient's device
            playSirenOnRecipientDevice(context)
        }
    }

    private fun playSirenOnRecipientDevice(context: Context?) {
        // Implement the logic to play the siren on the recipient's device
        // This might involve using notifications, alarms, or other means to get the user's attention.
        Log.d("SmsReceiver", "Playing siren on recipient's device")

        playSiren()
    }

    private fun playSiren() {
        if (!mediaPlayer.isPlaying) {
            // Start playing the siren sound
            mediaPlayer.start()
        }
    }
}

