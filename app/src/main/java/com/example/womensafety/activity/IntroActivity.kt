package com.example.womensafety.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.example.womensafety.R

class IntroActivity : AppCompatActivity() {

    private lateinit var signUpBtn : Button
    private lateinit var signInBtn : Button
    private lateinit var sharedPreferences: SharedPreferences
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initializeViews()

        sharedPreferences = getSharedPreferences(getString(R.string.preferences_file_name), MODE_PRIVATE)
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this, FirstActivity::class.java)
            startActivity(intent)
            finish()
        }

        signUpBtn.setOnClickListener {
            openSignUpActivity()
        }

        signInBtn.setOnClickListener {
            openSignInActivity()
        }
    }

    private fun openSignUpActivity() {
        val intent = Intent(this@IntroActivity, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun openSignInActivity() {
        val intent = Intent(this@IntroActivity, SignInActivity::class.java)
        startActivity(intent)

    }

    private fun initializeViews() {
        signUpBtn = findViewById(R.id.btn_sign_up_intro)
        signInBtn = findViewById(R.id.btn_sign_in_intro)
    }
}