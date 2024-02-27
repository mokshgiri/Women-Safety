package com.example.womensafety.utils

// FirebaseUtils.kt


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object FirebaseUtils {

    fun initializeFirebase(){
        val firebaseDatabase = FirebaseDatabase.getInstance().getReference("users")
        val firebaseAuth = FirebaseAuth.getInstance()

    }

//    fun getCurrentUserMessage(callback: (String) -> Unit) {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
//
//        currentUser?.uid?.let { userId ->
//            databaseReference.child(userId).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val currentUserName = snapshot.value?.toString() ?: ""
//                    val message = "My name is $currentUserName"
//                    callback(message)
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Handle error
//                }
//            })
//        }
//    }

    // You can add more utility methods for fetching other data from Firebase if needed.
}
