package com.example.womensafety.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.womensafety.receiver.PowerButtonReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

object LocationUtils {

    suspend fun getLocation(
        context: Context,
        activity: Activity,
        fusedLocationClient: FusedLocationProviderClient,
        callback: (Location) -> Unit,
        continuousCallback: (Location) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PowerButtonReceiver.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            try {
                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    callback.invoke(location)
                }
                else {
                    requestLocationUpdates(context, callback, continuousCallback, fusedLocationClient)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Location", "Error getting last location: ${exception.message}")
                    Toast.makeText(context, "Error getting location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun requestLocationUpdates(
        context: Context,
        callback: (Location) -> Unit,
        continuousCallback: (Location) -> Unit,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000) // Adjust this interval as needed
            .setFastestInterval(2000)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                callback.invoke(location!!)
                continuousCallback.invoke(location)
            }
        }

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null).await()
        } catch (exception: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("Location", "Error requesting location updates: ${exception.message}")
                Toast.makeText(context, "Error getting location updates", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setMaxZoom(googleMap: GoogleMap) {
        // Set the maximum zoom level for the GoogleMap
        val maxZoomLevel = 21.0f // Adjust this value as needed
        googleMap.setMaxZoomPreference(maxZoomLevel)
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun getGoogleMapsLink(location: Location): String {
        val latitude = location.latitude
        val longitude = location.longitude

        // Create a Google Maps URL with the latitude and longitude
        return "https://www.google.com/maps?q=$latitude,$longitude"
    }

    suspend fun getAddressFromLocationUrl(context: Context, locationUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Extract latitude and longitude from the Google Maps URL
                val uri = Uri.parse(locationUrl)
                val latitude = uri.getQueryParameter("q")?.split(",")?.get(0)?.toDoubleOrNull()
                val longitude = uri.getQueryParameter("q")?.split(",")?.get(1)?.toDoubleOrNull()

                if (latitude != null && longitude != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
                    if (addresses.isNotEmpty()) {
                        addresses[0].getAddressLine(0) ?: "No address found"
                    } else {
                        "Unable to retrieve address"
                    }
                } else {
                    "Invalid location URL"
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("Geocoding", "Error getting address: ${e.message}")
                    "Error getting address"
                }
            }
        }
    }
}
