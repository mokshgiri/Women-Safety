package com.example.womensafety.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.example.womensafety.R
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var etEmail: AppCompatEditText
    private lateinit var btnSignIn: Button
    private lateinit var etPassword: AppCompatEditText
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initializeViews()
        initializeFirebase()
        setUpActionbar()

        btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }

        sharedPreferences = getSharedPreferences(getString(R.string.preferences_file_name), MODE_PRIVATE)

    }

    private fun savePreferences() {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar_sign_in_activity)
        btnSignIn = findViewById(R.id.btn_sign_in)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
    }


    private fun setUpActionbar() {
        setSupportActionBar(toolbar)

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_back_icon)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    private fun signInRegisteredUser() {
        val email = etEmail.text.toString().trim() { it <= ' ' }
        val pass = etPassword.text.toString().trim() { it <= ' ' }

        if (validateForm(email,pass)){
            showProgressDialog(resources.getString(R.string.please_wait))
            firebaseAuth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        hideProgressDialog()
                        savePreferences()
                        finish()

                        val intent = Intent(this, FirstActivity::class.java)
                        intent.putExtra("email",email)
                        startActivity(intent)
//                        intent.putExtra("email",email)
//                        FirestoreClass().loadUserData(this@SignInActivity)

                    }
                    else{
                        hideProgressDialog()
                        Toast.makeText(this@SignInActivity, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter your email")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter your password")
                false
            }

            else -> {
                true
            }
        }
    }
}
