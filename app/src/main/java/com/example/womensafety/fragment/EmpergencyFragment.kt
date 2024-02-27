package com.example.womensafety.fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.womensafety.receiver.PowerButtonReceiver
import com.example.womensafety.R
import com.example.womensafety.activity.BaseActivity
import com.example.womensafety.activity.RecordingsActivity
import com.example.womensafety.receiver.RecordingService
import com.example.womensafety.utils.LocationUtils
import com.example.womensafety.utils.MyAnimations
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class EmpergencyFragment : Fragment() {

    private lateinit var btn: CircleImageView
    private lateinit var sendBtn: Button
    private lateinit var message: AppCompatEditText
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var speechIntent : Intent
    private lateinit var locationIntent : Intent
    private lateinit var progressDialog: Dialog
    private lateinit var tvProgressText: TextView
    private lateinit var address: String
    private lateinit var alertWhatsappBtn: Button
//    private var volumeDownCount: Int = 0
//    private var lastVolumeDownTime: Long = 0
    private var permissionsGranted: Boolean = false
    private lateinit var allRecordingsBtn : Button
    private lateinit var recordingIntent : Intent
    private lateinit var auth: FirebaseAuth
    private lateinit var animation: Animation
    private lateinit var serviceIntent: Intent

//    private var currentUser: FirebaseUser ?= null
//    private var userId: String ?= null
//    private var mediaRecorder: MediaRecorder? = null
//    private var mediaPlayer: MediaPlayer? = null
//    private val storage = Firebase.storage
//    private lateinit var storageRef: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            initLocationManager()
            requestPermissionsSms(requireContext())
//            initializeFirebase()


            withContext(Dispatchers.Main) {
                // Switch back to the main thread to perform UI-related tasks

            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

//        initializeProgressDialog()
        initializeIntents()
    }

//    private fun initializeProgressDialog() {
//        progressDialog = Dialog(requireContext())
//        progressDialog.setContentView(R.layout.dialog_progress)
//        progressDialog.setCancelable(false)
//        tvProgressText = progressDialog.findViewById(R.id.tv_progress_text)
//    }

    private fun initializeIntents() {
        locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        serviceIntent = Intent(requireContext(), RecordingService::class.java)
        recordingIntent = Intent(requireContext(), RecordingsActivity::class.java)
    }

//    private fun initializeFirebase(){
//        auth = FirebaseAuth.getInstance()
//        currentUser = auth.currentUser
//        userId = currentUser?.uid
//        storageRef = storage.reference.child("audios/$userId")
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        btn = view.findViewById(R.id.btn)
        sendBtn = view.findViewById(R.id.send)
        message = view.findViewById(R.id.msg)
        alertWhatsappBtn = view.findViewById(R.id.alertWhatsappBtn)
        allRecordingsBtn = view.findViewById(R.id.allRecordings)



//        initializeProgressDialog()


        btn.setOnLongClickListener {
            MyAnimations.startMicrophoneAnimation(requireContext(), btn)
            Handler().postDelayed({
                if (permissionsGranted) {
                    if (!LocationUtils.isLocationEnabled(requireContext())) {
                        showEnableLocationDialog()
                    } else {
                        startSpeechRecognition()
                            startRecording()
                    }
                }
            }, 500)
            true
        }

        btn.setOnClickListener {
            stopRecording()
        }

        sendBtn.setOnClickListener {
            if (message.text.toString().isNotEmpty()) {
                GlobalScope.launch {
                    handleButtonClick(true)
                }
            } else {
//                hideProgressDialog()
//                Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
                (activity as? BaseActivity)?.showErrorSnackBar("Please enter a message!!")
            }
        }
        
        alertWhatsappBtn.setOnClickListener {
//            GlobalScope.launch {
                handleButtonClick(false)
//            }
        }

        allRecordingsBtn.setOnClickListener {
            startActivity(recordingIntent)
        }


        return view
    }

    private fun handleButtonClick(sendMessage: Boolean) {
        if (permissionsGranted) {
            if (!LocationUtils.isLocationEnabled(requireContext())) {
                showEnableLocationDialog()
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    showProgressDialog("Please wait")
                }


                GlobalScope.launch() {
                    LocationUtils.getLocation(
                        requireContext(),
                        requireActivity(),
                        fusedLocationClient,
                        { location ->
                            val urlLocation = LocationUtils.getGoogleMapsLink(location)
                            lifecycleScope.launch(Dispatchers.Main) {
                                address =
                                    LocationUtils.getAddressFromLocationUrl(
                                        requireContext(),
                                        urlLocation
                                    )

                                // Move the code related to sending a WhatsApp message to a background thread
                                lifecycleScope.launch(Dispatchers.Default) {
                                    if (sendMessage) {
                                        PowerButtonReceiver().performDesiredAction(
                                            requireContext(),
                                            message.text.toString(),
                                            urlLocation,
                                            address
                                        )
                                        message.text!!.clear()
                                    } else {
                                        PowerButtonReceiver().sendMessageToWhatsApp(
                                            requireContext(),
                                            urlLocation,
                                            address
                                        )
                                    }

                                    // Hide the progress dialog on the main thread
                                    launch(Dispatchers.Main) {
                                        hideProgressDialog()
                                    }
                                }
                            }
                        },
                        { _ -> }
                    )
                }
            }
        } else {
            requestPermissionsSms(requireContext())
        }
    }



    private fun showEnableLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Location services are disabled. Please enable them to use this feature.")
            .setPositiveButton("Settings") { _, _ ->
//                locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(locationIntent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private suspend fun initLocationManager() {
        withContext(Dispatchers.Default) {
            locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {}
                override fun onProviderDisabled(provider: String) {
                    Toast.makeText(
                        context,
                        "GPS provider disabled. Enable GPS and try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            }
        }
    }

    private fun requestPermissionsSms(context: Context) {
        val permissionsList = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Add notification permission for Android 12 and later
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Filter out null or empty permissions
        val nonNullOrEmptyPermissions = permissionsList.filter { it.isNotEmpty() }

        Dexter.withContext(context)
            .withPermissions(*nonNullOrEmptyPermissions.toTypedArray())
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.areAllPermissionsGranted()) {
                        permissionsGranted = true
                    } else {
                        showRationalDialogsForPermission(context)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1!!.continuePermissionRequest()
                }
            })
            .withErrorListener {
                Toast.makeText(context, it.name, Toast.LENGTH_SHORT).show()
            }
            .onSameThread()
            .check()
    }

    private fun showRationalDialogsForPermission(context: Context) {
        AlertDialog.Builder(context)
            .setMessage("It looks like you have denied the permission. Please enable the permissions.")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    private fun startSpeechRecognition() {
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        startActivityForResult(speechIntent, PowerButtonReceiver.SPEECH_REQUEST_CODE)
        Toast.makeText(context, "Shout HELP HELP two times !", Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PowerButtonReceiver.SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            matches?.let {
                GlobalScope.launch {
                    LocationUtils.getLocation(requireContext(),
                        requireActivity(),
                        fusedLocationClient,
                        { location ->
                            val urlLocation = LocationUtils.getGoogleMapsLink(location)
                            lifecycleScope.launch(Dispatchers.Main) {
                                address =
                                    LocationUtils.getAddressFromLocationUrl(
                                        requireContext(),
                                        urlLocation
                                    )
                                Handler().post {
                                    PowerButtonReceiver().processSpeechResults(
                                        requireContext(),
                                        it,
                                        urlLocation,
                                        address
                                    )
                                }
                            }
                        },
                        { updatedLocation -> }
                    )
                }
            }
        }
    }
//
//    fun handleVolumeDownButton() {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastVolumeDownTime < PowerButtonReceiver.VOLUME_DOWN_BUTTON_INTERVAL) {
//            volumeDownCount++
//            if (volumeDownCount >= PowerButtonReceiver.VOLUME_DOWN_BUTTON_PRESS_COUNT) {
//                volumeDownCount = 0
//                if (permissionsGranted) {
//                    GlobalScope.launch {
//                        LocationUtils.getLocation(requireContext(),
//                            requireActivity(),
//                            fusedLocationClient,
//                            { location ->
//                                val urlLocation = LocationUtils.getGoogleMapsLink(location)
//                                Handler().post {
//                                    PowerButtonReceiver().performDesiredAction(
//                                        requireContext(),
//                                        "null",
//                                        urlLocation,
//                                        address
//                                    )
//                                }
//                            },
//                            { updatedLocation -> }
//                        )
//                    }
//                }
//                else {
//                requestPermissionsSms(requireContext())
//                }
//            }
//        } else {
//            volumeDownCount = 1
//        }
//        lastVolumeDownTime = currentTime
//    }

    fun showProgressDialog(text: String) {
        (activity as? BaseActivity)?.showProgressDialog(text)
//        tvProgressText.text = text
//        progressDialog.show()
    }

    fun hideProgressDialog() {
        (activity as? BaseActivity)?.hideProgressDialog()
    }



    private fun startRecording() {
        startRecordingService()
    }

//    private fun releaseMediaRecorder() {
//        mediaRecorder?.apply {
//            reset()
//            release()
//        }
//        mediaRecorder = null
//
//    }

    private fun stopRecording() {
        try {

            stopRecordingService()


        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }

    }

//    private fun playRecording() {
//        try{
//            mediaPlayer = MediaPlayer()
//            mediaPlayer!!.setDataSource(getOutputMediaFile())
//            mediaPlayer!!.prepare()
//            mediaPlayer!!.start()
//        }
//        catch (e : Exception){
//            e.printStackTrace()
//        }
//    }

//    private fun getOutputMediaFile(): String{
//        // Get the internal storage directory
////        val mediaStorageDir = File(requireContext().filesDir, "audio_recordings")
//
//        val contextWrapper = ContextWrapper(requireContext())
//        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
//        val file = File(musicDirectory, "testRecordingFile" + "mp3")
//        return file.path
//
//    }

    private fun startRecordingService() {
        requireContext().startService(serviceIntent)
    }

    private fun stopRecordingService() {
        requireContext().stopService(serviceIntent)
    }


//
//    fun onPlayRecordingClick(recordingPath: String) {
//        val mediaPlayer = MediaPlayer()
//
//        try {
//            mediaPlayer.setDataSource(recordingPath)
//            mediaPlayer.prepare()
//            mediaPlayer.start()
//
//            // Optionally, you can set up an event listener to handle when the playback is completed
//            mediaPlayer.setOnCompletionListener {
//                // Implement your logic when audio playback is completed
//                // For example, update UI, show a message, etc.
//            }
//
//        } catch (e: IOException) {
//            // Handle exceptions (e.g., file not found, invalid file format)
//            Log.e("MediaPlayer", "Error playing recording: ${e.message}")
//        }
//    }



//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(TAG, "Service destroyed")
//        stopRecording()
////        releaseMediaPlayer()
//    }



}
