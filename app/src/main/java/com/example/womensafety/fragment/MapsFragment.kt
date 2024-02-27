package com.example.womensafety.fragment
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.womensafety.R
import com.example.womensafety.firebase.RealtimeDatabaseClass
import com.example.womensafety.utils.LocationUtils
import com.example.womensafety.utils.MyAnimations
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var refreshBtn: CircleImageView
    private lateinit var address: String

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineJob)

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        refreshBtn = view.findViewById(R.id.refresh)
        initializeMapFragment()
        setRefreshButtonListener()

        return view
    }

    private fun initializeMapFragment() {
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setRefreshButtonListener() {
        refreshBtn.setOnClickListener {
            GlobalScope.launch {
                startLocationUpdates()
            }
            MyAnimations.startRefreshButtonAnimation(requireContext(), refreshBtn)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        LocationUtils.setMaxZoom(mMap)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            coroutineScope.launch {
                startLocationUpdates()
            }
        }

        if (!LocationUtils.isLocationEnabled(requireContext())) {
            showEnableLocationDialog()
        }
    }


    private fun showEnableLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Location services are disabled. Please enable them to use this feature.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private suspend fun startLocationUpdates() {
        LocationUtils.getLocation(
            requireContext(),
            requireActivity(),
            fusedLocationClient,
            { location ->
                lifecycleScope.launch {
                    RealtimeDatabaseClass().updateLocationInFirebase(this@MapsFragment, location.toString())
                    updateLocationOnMap(location)
                }
            },
            { updatedLocation ->
                RealtimeDatabaseClass().updateLocationInFirebase(this, updatedLocation.toString())
                addMarkerForLocation(updatedLocation)
            }
        )
    }

    private suspend fun updateLocationOnMap(location: Location) {
        val urlLocation = LocationUtils.getGoogleMapsLink(location)
        address = LocationUtils.getAddressFromLocationUrl(requireContext(), urlLocation)

        val currentLocation = LatLng(location.latitude, location.longitude)
        mMap.addMarker(
            MarkerOptions().position(currentLocation)
                .title("Current Location: $address")
        )
        val latLngZoom = CameraUpdateFactory.newLatLngZoom(currentLocation, 21.0f)
        mMap.animateCamera(latLngZoom)
    }

    private fun addMarkerForLocation(location: Location) {
        mMap.clear()

        val newLocation = LatLng(location.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(newLocation).title("Updated Location"))
    }
}
