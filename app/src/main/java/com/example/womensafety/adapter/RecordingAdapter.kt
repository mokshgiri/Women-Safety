package com.example.womensafety.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafety.R
import com.example.womensafety.activity.RecordingsActivity
import com.example.womensafety.model.RecordingItem
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
class RecordingAdapter(private val activity: RecordingsActivity) :
    ListAdapter<RecordingItem, RecordingAdapter.RecordingViewHolder>(RecordingDiffCallback()) {

    private val storage = Firebase.storage
    private var currentlyPlayingPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recording_item, parent, false)
        return RecordingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val recording = getItem(position)
        holder.bind(recording)
    }

    inner class RecordingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val playButton: Button = itemView.findViewById(R.id.buttonPlay)
        private val stopButton: Button = itemView.findViewById(R.id.buttonStop)
        private val currentPlayingTxt: TextView = itemView.findViewById(R.id.currentPlayingText)
        private val wholeItem: LinearLayout = itemView.findViewById(R.id.childLinearLayout)

        fun bind(recording: RecordingItem) {
            titleTextView.text = recording.title

            // Handle playback when the play button is clicked
            playButton.setOnClickListener {
                if (layoutPosition != currentlyPlayingPosition) {
                    // Play the recording only if it's not already playing
                    activity.releaseMediaPlayer()
//                    playRecording(recording)
                    setCurrentlyPlaying(layoutPosition)
                }
            }



//            // Update visibility based on the current playing position
//            currentPlayingTxt.visibility =
                if (layoutPosition == currentlyPlayingPosition) {
                   currentPlayingTxt.visibility = View.VISIBLE
                    stopButton.visibility = View.VISIBLE
                }
                else{
                    currentPlayingTxt.visibility = View.GONE
                    stopButton.visibility = View.INVISIBLE
                }

            stopButton.setOnClickListener {
                setCurrentlyPlaying(RecyclerView.NO_POSITION)
                activity.releaseMediaPlayer()
            }

        }


        // Function to set the currently playing position
        private fun setCurrentlyPlaying(position: Int) {
            val previousPlayingPosition = currentlyPlayingPosition
            currentlyPlayingPosition = position

            Log.d("currentPlayingPos", currentlyPlayingPosition.toString())

            // Update the visibility of the previous playing item
            notifyItemChanged(previousPlayingPosition)

            // Update the visibility of the current playing item
            notifyItemChanged(currentlyPlayingPosition)

            activity.releaseMediaPlayer()
        }

        private fun playRecording(recording: RecordingItem) {
            val storageRef = storage.reference.child(recording.filePath)

            // Download the audio file from Firebase Storage
            CoroutineScope(Dispatchers.IO).launch {
                val localFile = createLocalFile(recording.title)

                Log.d("localFilePath", localFile.path)
                storageRef.getFile(localFile)
                    .addOnSuccessListener {
                        // Successfully downloaded the file, play it
//                        activity.onPlayRecordingClick(localFile.path)
                    }
                    .addOnFailureListener { exception ->
                        // Handle failures, such as a file not found
                        // You can show a message or log the error
                    }
            }
        }

        // This function creates a local file to store the downloaded audio file
        private fun createLocalFile(fileName: String): File {
            val context = itemView.context
            val cacheDir = context.externalCacheDir ?: context.cacheDir


            return File(cacheDir, fileName)
        }
    }

    class RecordingDiffCallback : DiffUtil.ItemCallback<RecordingItem>() {
        override fun areItemsTheSame(oldItem: RecordingItem, newItem: RecordingItem): Boolean {
            // Check if items have the same ID or unique identifier
            return oldItem.filePath == newItem.filePath
        }

        override fun areContentsTheSame(oldItem: RecordingItem, newItem: RecordingItem): Boolean {
            // Check if the contents of the items are the same
            return oldItem == newItem
        }
    }
}
