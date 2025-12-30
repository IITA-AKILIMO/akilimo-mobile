package com.akilimo.mobile.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentLocationBinding
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.ui.activities.LocationPickerActivity
import com.akilimo.mobile.utils.LocationHelper
import com.akilimo.mobile.utils.PermissionHelper
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [LocationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocationFragment : BaseStepFragment<FragmentLocationBinding>() {

    companion object {
        private const val LOCATION_FORMAT = "Lat: %.5f, Lng: %.5f"
        private const val LOCATION_PICKER_REQUEST = 1001

        fun newInstance() = LocationFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo
    private lateinit var locationHelper: LocationHelper
    private lateinit var permissionHelper: PermissionHelper

    private val locationPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleLocationPickerResult(result.data)
            }
        }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            fetchCurrentLocation()
        } else {
            showLocationPermissionDenied()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLocationBinding = FragmentLocationBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        initializeDependencies()
        setupViewListeners()
        prefillFromEntity()
    }

    private fun initializeDependencies() {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())
        locationHelper = LocationHelper()
        permissionHelper = PermissionHelper()
    }

    private fun setupViewListeners() {
        binding.btnUseCurrentLocation.setOnClickListener {
            handleCurrentLocationClick()
        }

        binding.btnSelectLocationManually.setOnClickListener {
            launchLocationPicker()
        }

        binding.btnSelectFarmName.setOnClickListener {
            showFarmNameFeature()
        }
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser)
            user?.let { displayUserLocation(it) }
        }
    }

    private fun handleCurrentLocationClick() {
        if (permissionHelper.hasLocationPermission(requireContext())) {
            fetchCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun fetchCurrentLocation() {
        safeScope.launch {
            when (val locationResult = locationHelper.getCurrentLocation(requireContext())) {
                is LocationHelper.LocationResult.Success -> {
                    val location = locationResult.location
                    updateLocationInfo(
                        lat = location.latitude,
                        lng = location.longitude
                    )
                }

                is LocationHelper.LocationResult.Error -> {
                    showToast(locationResult.message)
                    handleLocationError()
                }

                is LocationHelper.LocationResult.LocationDisabled -> {
                    showLocationServicesDialog()
                }

                LocationHelper.LocationResult.PermissionDenied -> {
                    showLocationPermissionDenied()
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (shouldShowPermissionRationale()) {
            showPermissionRationaleDialog {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun launchLocationPicker() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser)
            val intent = Intent(requireContext(), LocationPickerActivity::class.java).apply {
                user?.let {
                    putExtra(LocationPickerActivity.LAT, it.latitude)
                    putExtra(LocationPickerActivity.LON, it.longitude)
                    putExtra(LocationPickerActivity.ZOOM, it.zoomLevel)
                    putExtra(LocationPickerActivity.ALT, it.altitude)
                }
            }
            locationPickerLauncher.launch(intent)
        }
    }

    private fun handleLocationPickerResult(data: Intent?) {
        data?.let {
            val lat = it.getDoubleExtra(LocationPickerActivity.LAT, 0.0)
            val lng = it.getDoubleExtra(LocationPickerActivity.LON, 0.0)
            val zoom = it.getDoubleExtra(LocationPickerActivity.ZOOM, 12.0)

            if (isValidLocation(lat, lng)) {
                updateLocationInfo(lat, lng, zoom)
            } else {
                showToast("Invalid location coordinates")
            }
        }
    }

    private fun updateLocationInfo(
        lat: Double,
        lng: Double,
        zoom: Double = 12.0
    ) {
        displayLocationText(lat, lng)
        saveUserLocation(lat, lng, zoom)
    }

    private fun displayLocationText(lat: Double, lng: Double) {
        binding.textLocationInfo.text = LOCATION_FORMAT.format(lat, lng)
    }

    private fun displayUserLocation(user: AkilimoUser) {
        if (isValidLocation(user.latitude, user.longitude)) {
            displayLocationText(user.latitude, user.longitude)
        }
    }

    private fun saveUserLocation(lat: Double, lng: Double, zoom: Double) {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: createNewUser()
            val updatedUser = user.copy(
                latitude = lat,
                longitude = lng,
                zoomLevel = zoom
            )
            userRepository.saveOrUpdateUser(updatedUser, sessionManager.akilimoUser)
        }
    }

    private fun createNewUser(): AkilimoUser {
        return AkilimoUser(userName = sessionManager.akilimoUser)
    }

    private fun isValidLocation(lat: Double, lng: Double): Boolean {
        return lat != 0.0 || lng != 0.0
    }

    private fun shouldShowPermissionRationale(): Boolean {
        return permissionHelper.shouldShowPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun showPermissionRationaleDialog(onContinue: () -> Unit) {
        // Show a dialog explaining why location permission is needed
        // For now, just continue with permission request
        onContinue()
    }

    private fun showLocationServicesDialog() {
        // Show dialog to enable location services
        showToast("Please enable location services")
    }

    private fun showLocationPermissionDenied() {
        showToast("Location permission is required to use this feature")
    }

    private fun showFarmNameFeature() {
        showToast("Farm name selection coming soon")
    }

    private fun handleLocationError() {
        binding.textLocationInfo.text = "Location unavailable"
    }
}