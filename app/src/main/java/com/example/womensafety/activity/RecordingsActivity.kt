package com.example.womensafety.activity

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafety.R
import com.example.womensafety.adapter.RecordingAdapter
import com.example.womensafety.model.RecordingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class RecordingsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recordingAdapter: RecordingAdapter
    private val storage = Firebase.storage
    private lateinit var storageRef: StorageReference
    private lateinit var toolbar: Toolbar
    private var mediaPlayer : MediaPlayer ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)

        recyclerView = findViewById(R.id.recyclerView)

        toolbar = findViewById(R.id.toolbar)

        // Set up the RecyclerView and adapter
        recordingAdapter = RecordingAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recordingAdapter


        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        storageRef = storage.reference.child("audios/$userId")

        setUpActionbar()

        // Load recordings from Firebase Storage
        loadRecordingsFromFirebaseStorage()

    }

    private fun setUpActionbar() {
        setSupportActionBar(toolbar)

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_back_icon)
        actionbar?.title = "Back"

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_back_btn, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.action_delete_card ->{
////                alertDialogForDeleteList(boardDetails.taskList[taskListPosition].cards[cardPosition].name)
//                onBackPressed()
//                return true
//            }
//        }

//        return super.onOptionsItemSelected(item)
//    }

    private fun loadRecordingsFromFirebaseStorage() {

        showProgressDialog("Loading")

        lifecycleScope.launch(Dispatchers.IO) {
            val recordingsList = mutableListOf<RecordingItem>()


            // List all items (files) in the "audios" folder of Firebase Storage
            storageRef.listAll()
                .addOnSuccessListener { result ->
                    result.items.forEach { item ->
                        val title = item.name
                        val path = item.path

                        // Create a RecordingItem with title and path
                        val recordingItem = RecordingItem(title, path)

                        Log.d("recordingItem", recordingItem.toString())
                        recordingsList.add(recordingItem)
                    }

                    // Submit the list to the adapter on the main thread
//                    launch(Dispatchers.Main) {

                    recordingAdapter.submitList(recordingsList)

                    hideProgressDialog()

                    Log.d("recordingsList", recordingsList.toString())
//                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failures, such as unable to list items
                    // You can show a message or log the error
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            this@RecordingsActivity,
                            "Failed to load recordings from Firebase Storage",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    fun onPlayRecordingClick(recordingPath: String) {
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer?.setDataSource(recordingPath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()

            // Optionally, you can set up an event listener to handle when the playback is completed
            mediaPlayer?.setOnCompletionListener {
                // Implement your logic when audio playback is completed
                // For example, update UI, show a message, etc.
            }

        } catch (e: IOException) {
            // Handle exceptions (e.g., file not found, invalid file format)
            Log.e("MediaPlayer", "Error playing recording: ${e.message}")
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }


}