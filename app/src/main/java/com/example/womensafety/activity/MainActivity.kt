package com.example.womensafety.activity

import android.os.Bundle

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.speech.RecognizerIntent
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.womensafety.receiver.PowerButtonReceiver
import com.example.womensafety.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.PermissionToken


class MainActivity : Activity() {

    private lateinit var btn: Button
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var volumeDownCount: Int = 0
    private var lastVolumeDownTime: Long = 0

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.btn)
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_siren)


        // Initialize location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Initialize LocationManager and LocationListener
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Handle location updates if needed
            }

            override fun onProviderDisabled(provider: String) {
                // Handle provider disabled
                Toast.makeText(
                    this@MainActivity,
                    "GPS provider disabled. Enable GPS and try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onProviderEnabled(provider: String) {
                // Handle provider enabled
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                // Handle status changed
            }
        }
        requestPermissionsSms()

        btn.setOnClickListener {
            // Request permissions using Dexter
            requestPermissionsSms()
        }

//        startSpeechRecognition()
    }

    private fun requestPermissionsSms() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.areAllPermissionsGranted()) {
                        startSpeechRecognition()

                    } else {
                        showRationalDialogsForPermission()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?, p1: PermissionToken?) {
                    p1!!.continuePermissionRequest()
                }

            }).withErrorListener {
                Toast.makeText(this@MainActivity, it.name, Toast.LENGTH_SHORT).show()
            }.check()
    }


    private fun showRationalDialogsForPermission() {
        AlertDialog.Builder(this).setMessage("It looks like you have denied the permission. Please enable the permissions.")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        startActivityForResult(intent, PowerButtonReceiver.SPEECH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PowerButtonReceiver.SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            matches?.let {
                Log.d("matches", it.toString())
                // Pass the result to the PowerButtonReceiver for processing

                val location = getLocation()

//                if (location != null) {
//                    val urlLocation = getGoogleMapsLink(location)
//                    Log.d("location", urlLocation.toString())
//                    PowerButtonReceiver().processSpeechResults(this, it, urlLocation)
//                }

                playSiren()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Handle volume down button press
            handleVolumeDownButton()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun handleVolumeDownButton() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastVolumeDownTime < PowerButtonReceiver.VOLUME_DOWN_BUTTON_INTERVAL) {
            // Volume down button pressed within the specified interval
            volumeDownCount++

            if (volumeDownCount >= PowerButtonReceiver.VOLUME_DOWN_BUTTON_PRESS_COUNT) {
                // Reset the count for the next round
                volumeDownCount = 0

                // Trigger your desired action here
                Log.d("VolumeDown", "Volume down button pressed two times")

                val location = getLocation()
//
//                if (location != null) {
//                    val urlLocation = getGoogleMapsLink(location)
//                    Log.d("location", urlLocation.toString())
//                    PowerButtonReceiver().performDesiredAction(this, "null", urlLocation, address)
//                }
            }
        } else {
            // Reset the count if the time difference is greater than the interval
            volumeDownCount = 1
        }

        // Update the last volume down button pressed time
        lastVolumeDownTime = currentTime
    }

    private fun getLocation(): Location? {
        // Check location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PowerButtonReceiver.LOCATION_PERMISSION_REQUEST_CODE
            )
            return null
        }

        // Check if either GPS or network provider is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            // Prompt the user to enable location services
            // You can show a dialog or a toast to inform the user
            return null
        }

        // Request location updates and get the last known location
        var lastKnownLocation: Location? = null

        // Check GPS provider
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        // Check network provider if GPS provider did not return a location
        if (lastKnownLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        return lastKnownLocation
    }



    fun getGoogleMapsLink(location: Location): String {
        val latitude = location.latitude
        val longitude = location.longitude

        // Create a Google Maps URL with the latitude and longitude
        return "https://www.google.com/maps?q=$latitude,$longitude"
    }

    private fun playSiren() {
        // Check if the MediaPlayer is already playing
//        if (!mediaPlayer.isPlaying) {
//            // Start playing the siren sound
//            mediaPlayer.start()
//        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        // Release the MediaPlayer
//        mediaPlayer.release()
//    }



}

