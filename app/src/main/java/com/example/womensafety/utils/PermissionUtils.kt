package com.example.womensafety.utils// com.example.womensafety.utils.PermissionUtils.kt

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

object PermissionUtils {

    fun requestSmsPermission(context: Context, onPermissionGranted: () -> Unit) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.areAllPermissionsGranted()) {
                        onPermissionGranted.invoke()
                    } else {
                        showRationalDialogsForPermission(context)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1!!.continuePermissionRequest()
                }
            })
            .withErrorListener {
                // Handle error
            }
            .onSameThread()
            .check()
    }

    fun requestLocationPermission(context: Context, onPermissionGranted: () -> Unit) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.areAllPermissionsGranted()) {
                        onPermissionGranted.invoke()
                    } else {
                        showRationalDialogsForPermission(context)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1!!.continuePermissionRequest()
                }
            })
            .withErrorListener {
                // Handle error
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










//    var SMS_PERMISSIONS_GRANTED = false
//    var LOCATION_PERMISSIONS_GRANTED = false
//
//    private val SMS_PERMISSIONS = arrayOf(
//        Manifest.permission.RECEIVE_SMS,
//        Manifest.permission.READ_SMS,
//        Manifest.permission.SEND_SMS
//    )
//
//    private val LOCATION_PERMISSIONS = arrayOf(
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION
//    )
//
//    private val CONTACTS_PERMISSION = arrayOf(
//        Manifest.permission.READ_CONTACTS
//    )
//
//    private val RECORD_AUDIO = arrayOf(
//        Manifest.permission.RECORD_AUDIO
//    )
//
//    private val STORAGE_PERMISSION = arrayOf(
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
//    )
//
//
//    fun requestSmsPermission(context: Context) {
//        requestPermission(context, SMS_PERMISSIONS)
//    }
//
//    fun requestLocationPermission(context: Context) {
//        requestPermission(context, LOCATION_PERMISSIONS)
//    }
//
//    fun requestContactsPermission(context: Context, onPermissionGranted: () -> Unit) {
//        requestPermission(context, CONTACTS_PERMISSION)
//    }
//
//    fun requestRecordAudioPermission(context: Context, onPermissionGranted: () -> Unit) {
//        requestPermission(context, RECORD_AUDIO)
//    }
//
//    fun requestStoragePermission(context: Context, onPermissionGranted: () -> Unit) {
//        requestPermission(context, STORAGE_PERMISSION)
//    }
////
////    Manifest.permission.READ_CONTACTS,
////    Manifest.permission.RECORD_AUDIO,
////    Manifest.permission.WRITE_EXTERNAL_STORAGE
//
//    private fun requestPermission(
//        context: Context,
//        permissions: Array<String>
//    ) {
//        Dexter.withContext(context)
//            .withPermissions(*permissions)
//            .withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
//                    if (p0!!.areAllPermissionsGranted()) {
////                        onPermissionGranted.invoke()
//                        if (permissions.contentEquals(SMS_PERMISSIONS)) {
//                            SMS_PERMISSIONS_GRANTED = true
//                        }
//                        else if (permissions.contentEquals(LOCATION_PERMISSIONS)) {
//                            LOCATION_PERMISSIONS_GRANTED = true
//                        }
//
//                    } else {
//                        showRationalDialogsForPermission(context)
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
//                    p1: PermissionToken?
//                ) {
//                    p1!!.continuePermissionRequest()
//                }
//            })
//            .withErrorListener {
//                // Handle error
//            }
//            .onSameThread()
//            .check()
//    }
//
//    private fun showRationalDialogsForPermission(context: Context) {
//        AlertDialog.Builder(context)
//            .setMessage("It looks like you have denied the permission. Please enable the permissions.")
//            .setPositiveButton("GO TO SETTINGS") { _, _ ->
//                try {
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    val uri = Uri.fromParts("package", context.packageName, null)
//                    intent.data = uri
//                    context.startActivity(intent)
//                } catch (e: ActivityNotFoundException) {
//                    e.printStackTrace()
//                }
//            }.setNegativeButton("Cancel") { dialog, _ ->
//                dialog.dismiss()
//            }.show()
//    }
}
