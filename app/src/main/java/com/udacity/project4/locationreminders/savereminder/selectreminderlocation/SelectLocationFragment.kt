package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        const val REQUEST_TURN_LOCATION_ON = 1
    }

    // Map
    private var map: GoogleMap? = null
    private val TAG = SelectLocationFragment::class.java.simpleName

    // Location permission
    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (!it.values.contains(false)) {
                // All permissions are granted. Check device location
                checkDeviceLocationSettings()
            }
        }

    // User location and camera settings
    private var userLocationLong: Double = 0.0
    private var userLocationLat: Double = 0.0
    private val zoomLevel = 12.0f
    private var userMarker: Marker? = null

    // Fused Location API
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.locations.isNotEmpty()) {
                val location = locationResult.locations.last()

                lastLocation = location
                userLocationLong = location.longitude
                userLocationLat = location.latitude

                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(userLocationLat, userLocationLong),
                        zoomLevel
                    )
                )
            }
        }
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (map?.isMyLocationEnabled == true) {
            moveCameraToUserLocation()
        }

        // Confirm location FAB
        binding.confirmLocationFab.setOnClickListener {
            onLocationSelected(it)
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()

        // Stop location updates
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun onLocationSelected(view: View) {
        if (userMarker == null) {
            Snackbar.make(
                view,
                "Please select a location first.",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        _viewModel.reminderSelectedLocationStr.value = userMarker?.title
        _viewModel.latitude.value = userMarker?.position?.latitude
        _viewModel.longitude.value = userMarker?.position?.longitude
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        locationRequest = LocationRequest.Builder(180000L)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        setMarkerOnLongClick(map!!)
        setPoiClick(map!!)
        setMapStyle(map!!)
        requestForegroundPermissions()
    }

    /**
     * Tracks user location and enables "MyLocationEnabled" in the Google Map.
     * Moves the camera to the user location.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
        map?.isMyLocationEnabled = true
        moveCameraToUserLocation()
    }

    /**
     * Moves the camera to the location of the user if permissions are granted.
     */
    private fun moveCameraToUserLocation() {
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(userLocationLat, userLocationLong),
                zoomLevel
            )
        )
    }

    /**
     * Sets a marker on the map when the user does a long click.
     * If there is already a marker on the map it will remove it and place a new one.
     * @param map the map object it will use to set the marker.
     */
    private fun setMarkerOnLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            userMarker?.remove()
            userMarker = null

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )

            userMarker = map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            userMarker?.showInfoWindow()
        }
    }

    /**
     * Sets a marker on the Point Of Interest (POI) that the user clicked.
     * If there is already a marker on the map it will remove it and place a new one.
     * @param map the map object it will use to set the marker.
     */
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            userMarker?.remove()
            userMarker = null

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latLng.latitude,
                it.latLng.longitude
            )

            userMarker = map.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
                    .snippet(snippet)
            )
            userMarker?.showInfoWindow()
        }
    }

    /**
     * Tries to change the style of a google map to the one defined in "res/raw/map_style.json" file.
     * In case of a failure/error it sends a message to the log.
     * @param map the GoogleMap object you want to change its style.
     */
    private fun setMapStyle(map: GoogleMap) {
        try {
            val isStyleSet = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!isStyleSet) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find the style JSON file. Error: ${e.message}")
        }
    }

    /**
     * Checks the status of foreground permissions.
     * If granted it enables Google Maps My Location.
     * If not granted it shows permission rationale if necessary or asks the user for permission.
     */
    private fun requestForegroundPermissions() {
        when {
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
                // Both permissions are granted! Check device location
                checkDeviceLocationSettings()
            }

            (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )) -> {
                // Should show rationale for either fine or coarse
                Snackbar.make(
                    binding.root,
                    getString(R.string.permission_denied_explanation),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.ok) {
                    requestFineAndCoarsePermissions()
                }.show()
            }
            else -> {
                // Did not ask for permissions yet.
                requestFineAndCoarsePermissions()
            }
        }
    }

    /**
     * Requests fine and coarse location permissions from the user.
     */
    private fun requestFineAndCoarsePermissions() {
        requestMultiplePermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Checks the state of the location service of the device.
     * If disabled it shows a request to the user to enable it if he wishes to enable his location on the map.
     */
    fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest =
            LocationRequest.create().setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(this@SelectLocationFragment.requireActivity(),
                    REQUEST_TURN_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution ${sendEx.message}")
                }
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.location_required_error),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnSuccessListener {
            if (it.locationSettingsStates?.isLocationUsable == true) {
                enableMyLocation()
            }
        }
    }
}















