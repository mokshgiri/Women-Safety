package com.example.womensafety.firebase

import android.util.Log
import com.example.womensafety.activity.FirstActivity
import com.example.womensafety.activity.SignUpActivity
import com.example.womensafety.fragment.MapsFragment
import com.example.womensafety.fragment.ProfileFragment
import com.example.womensafety.fragment.SettingsFragment
import com.example.womensafety.model.Users
import com.example.womensafety.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class RealtimeDatabaseClass {
    private val database = FirebaseDatabase.getInstance().getReference(Constants.USERS)

    private val userRef = database.child(getCurrentUserId())
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(activity : SignUpActivity, userInfo : Users){
        userRef.setValue(userInfo).addOnSuccessListener {
            // Data uploaded successfully
            activity.userRegisteredSuccess()
        }
    }

    fun getUserData(fragment: ProfileFragment){
        userRef.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fragment.hideProgressDialog()
                if (snapshot.exists()) {
                    val userProfile = snapshot.getValue(Users::class.java)
                    userProfile?.let {
                        fragment.updateUIWithUserData(it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                fragment.hideProgressDialog()
            }
        })
    }

    fun updateLocationInFirebase(fragment: MapsFragment, updatedLocation : String) {

        userRef.child("location").setValue(updatedLocation)
    }

    fun getCurrentUserMessage(callback: (String) -> Unit) {

        getCurrentUserId().let { userId ->

            Log.d("userrid", userId)
            userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUserName = snapshot.value?.toString() ?: ""
                    val message = "My name is $currentUserName"
                    callback(message)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun signOut(fragment : SettingsFragment){
        firebaseAuth.signOut()
    }

    private fun getCurrentUserId(): String{

        return FirebaseAuth.getInstance().currentUser!!.uid
    }

}