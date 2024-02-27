package com.example.womensafety.activity

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.womensafety.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false
    private lateinit var progressDialog : Dialog
    private lateinit var tvProgressText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

//        showProgressDialog(text = text)
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

    fun getCurrentUserId() : String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true

        Toast.makeText(this@BaseActivity, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()

        Handler().postDelayed({doubleBackToExitPressedOnce = false}, 2000)
    }

    fun showErrorSnackBar(message : String){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),
            message, Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
//        snackBarView.resources.getColor((R.color.snackbar_error_color))
        snackBarView.background = resources.getDrawable(R.drawable.shape_button_rounded_red)

//            (ContextCompat.getColor(this@BaseActivity,
//            R.drawable.shape_button_rounded_red
//        ))

        snackBar.show()
    }
}
