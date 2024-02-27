package com.example.womensafety.receiver

import android.app.Dialog
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.provider.Settings.Global
import android.util.Log
import android.util.Size
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.womensafety.R
import com.example.womensafety.activity.FirstActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class RecordingService : Service() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording: Boolean = false
    private lateinit var outputFile: String
    private val storage = Firebase.storage
    private var successfulUploadCount = 0

    private lateinit var progressDialog : Dialog
    private lateinit var tvProgressText : TextView

    private val audioFilePath: String by lazy {
        "${applicationContext?.externalCacheDir?.absolutePath}/audio_record.3gp"
    }

    companion object {
        const val CHANNEL_ID = "RecordingServiceChannel"
        private const val TAG = "RecordingService"
        private const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        // Initialize MediaRecorder when the service is created
        initMediaRecorder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        startRecording()

        // Start the service in the foreground
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, FirstActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording Service")
            .setContentText("Recording in progress")
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun initMediaRecorder() {
        outputFile = getOutputMediaFile()
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(TAG, "Failed to prepare MediaRecorder: ${e.message}")
                releaseMediaRecorder()
            }
        }
    }

    private fun releaseMediaRecorder() {
        mediaRecorder?.apply {
            reset()
            release()
        }
        mediaRecorder = null
        isRecording = false
    }

    private fun startRecording() {
        // Set the output file for MediaRecorder
        outputFile = getOutputMediaFile()

        if (outputFile != null) {
            // Initialize MediaRecorder and start recording
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)

                try {
                    prepare()
                    start()
                    isRecording = true
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to prepare MediaRecorder: ${e.message}")
                    releaseMediaRecorder()
                }
            }
        } else {
            Log.e(TAG, "Failed to create output media file")
        }
    }

    private fun stopRecording() {
        // Inside stopRecording function
        try {
            // ... (other code)
//            GlobalScope.launch(Dispatchers.IO) {
//                uploadRecordingsFromInternalStorage()

                // Perform UI operations on the main thread
//                launch(Dispatchers.Main) {
                    // You can now send the recorded audio file (audioFilePath) along with the message
                    // handleButtonClick(true)
                    Toast.makeText(applicationContext, audioFilePath, Toast.LENGTH_SHORT).show()
                    Log.d("recordingg", audioFilePath)
                    Toast.makeText(applicationContext, "Recording stopped", Toast.LENGTH_SHORT).show()

                    // Release MediaPlayer
                    releaseMediaPlayer()
//                }
//            }
            // ... (other code)
        } catch (e: Exception) {
            e.printStackTrace()

            // Perform UI operations on the main thread
//            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Failed to stop recording", Toast.LENGTH_SHORT).show()
//            }
        }
    }

    private fun playRecording() {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(outputFile)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getOutputMediaFile(): String {
        // Get the internal storage directory
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)

        // Generate a unique filename for each recording
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(musicDirectory, "audio_record_$timestamp.3gp")

        return file.path
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }

    fun uploadRecordingsFromInternalStorage(): List<String> {

//        showProgressDialog("Saving")
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)

        // List all files in the directory
        val files = musicDirectory?.listFiles { _, name ->
            name.endsWith(".3gp") // Adjust the file extension accordingly
        }

        if (files != null) {
            for(file in files){
                Log.d("fileRecorded", file.toString())

            }
        }

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val fileList = files?.map { it.absolutePath } ?: emptyList()

        for (filePath in fileList) {
            userId?.let {
                uploadAudioFile(it, filePath, fileList.size)
            }
        }

        // Extract file paths from the list of files
        return files?.map { it.absolutePath } ?: emptyList()
    }



    fun uploadAudioFile(userId: String, audioFilePath: String, fileListSize: Int) {

            GlobalScope.launch {
            val file = Uri.fromFile(File(audioFilePath))

            // Create a reference to "audios" folder with a unique filename
            val audioRef =
                storage.reference.child("audios/$userId/${System.currentTimeMillis()}.3gp")

//        first deleting all then reuploading
            val folderPath = "audios/$userId"
//            deleteAllFilesInFolder(folderPath)

            // Upload the file to Firebase Storage
            audioRef.putFile(file)
                .addOnSuccessListener { snapshot ->
                    // Handle successful upload
                    val downloadUrl = snapshot.metadata?.reference?.downloadUrl
//                Toast.makeText(applicationContext, "Recording saved successfully", Toast.LENGTH_SHORT).show()
                    val downloadUrlTask = audioRef.downloadUrl
                    downloadUrlTask.addOnSuccessListener { uri ->

                        val downloadUrl1 = uri.toString()
                        Log.d("downloadUrllAndFile", "$downloadUrl1 $file")
                        // Now, you can store downloadUrl in your Firebase Database.
                    }

                    // Increment the successful upload count
                    successfulUploadCount++

                    // Check if all recordings are uploaded
                    if (successfulUploadCount == fileListSize) {
                        // All recordings are uploaded, show the final success message
                        Toast.makeText(
                            applicationContext,
                            "All recordings saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()

//                    GlobalScope.launch(Dispatchers.Main) {
//                        hideProgressDialog()
//                    }
                    }

                    Log.d("FirebaseStorage", "Upload success. Download URL: $downloadUrl")

                }
                .addOnFailureListener { exception ->
                    // Handle failed upload
                    Log.e("FirebaseStorage", "Upload failed: ${exception.message}", exception)
                }
        }
//        }
    }


    fun deleteAllFilesInFolder(folderPath: String) {
        val storageRef = storage.reference.child(folderPath)

        // List all items (files and possibly subfolders) in the folder
        storageRef.listAll()
            .addOnSuccessListener { result ->
                result.items.forEach { item ->
                    // Delete each file
                    item.delete()
                        .addOnSuccessListener {
                            Log.d("FirebaseStorage", "File deleted: ${item.path}")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "FirebaseStorage",
                                "Failed to delete file: ${item.path}",
                                exception
                            )
                        }
                }

                // If there are subfolders, you may want to recursively delete their contents
                result.prefixes.forEach { prefix ->
                    deleteAllFilesInFolder(prefix.path)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseStorage", "Failed to list files for deletion", exception)
            }
    }

    fun showProgressDialog(text : String) {
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.dialog_progress)
        progressDialog.setCancelable(false)

        tvProgressText = progressDialog.findViewById(R.id.tv_progress_text)

        tvProgressText.text = text

        progressDialog.show()
    }

    fun hideProgressDialog(){
        progressDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        stopRecording()
    }

}
