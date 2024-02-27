package com.example.womensafety.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.womensafety.R
import com.example.womensafety.activity.IntroActivity
import com.example.womensafety.firebase.RealtimeDatabaseClass

class SettingsFragment : Fragment() {

    private lateinit var btnLogout : Button
    private lateinit var sharedPreferences: SharedPreferences
    private var isLoggedIn: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        btnLogout = view.findViewById(R.id.btnLogout)
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preferences_file_name),
            AppCompatActivity.MODE_PRIVATE
        )

        btnLogout.setOnClickListener {
            requireActivity().finish()
//            isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
//
            RealtimeDatabaseClass().signOut(this)

            val intent = Intent(context, IntroActivity::class.java)
            startActivity(intent)

            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
        }

        return view
    }

}