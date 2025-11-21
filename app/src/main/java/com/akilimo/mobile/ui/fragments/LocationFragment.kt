package com.akilimo.mobile.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentLocationBinding
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.ui.activities.LocationPickerActivity
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [LocationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocationFragment : BaseStepFragment<FragmentLocationBinding>() {

    companion object {
        fun newInstance() = LocationFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo

    private val locationPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val lat = result.data?.getDoubleExtra(LocationPickerActivity.LAT, 0.0)
                val lng = result.data?.getDoubleExtra(LocationPickerActivity.LON, 0.0)
                val alt = result.data?.getDoubleExtra(LocationPickerActivity.ALT, 0.0)
                val zoom = result.data?.getDoubleExtra(LocationPickerActivity.ZOOM, 12.0)
                if (lat != null && lng != null && alt != null && zoom != null) {
                    updateLocationInfo(lat, lng, alt, zoom)
                }
            }
        }


    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLocationBinding = FragmentLocationBinding.inflate(inflater, container, false)

    /**
     * Subclasses must implement this. Called after binding is safely initialized.
     */
    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())
        binding.btnUseCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnSelectLocationManually.setOnClickListener {
            lifecycleScope.launch {

                val user = userRepository.getUser(sessionManager.akilimoUser)
                val intent = Intent(requireContext(), LocationPickerActivity::class.java)

                user?.let {
                    val lat = it.latitude
                    val lng = it.longitude
                    val alt = it.altitude
                    val zoom = it.zoomLevel
                    intent.putExtra(LocationPickerActivity.LAT, lat)
                    intent.putExtra(LocationPickerActivity.LON, lng)
                    intent.putExtra(LocationPickerActivity.ZOOM, zoom)
                    intent.putExtra(LocationPickerActivity.ALT, alt)
                }
                locationPickerLauncher.launch(intent)
            }
        }

        binding.btnSelectFarmName.setOnClickListener {
            showToast("Farm name selection coming soon")
        }
    }


    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser)
            user?.let {
                val lat = it.latitude
                val lng = it.longitude
                val formatted = "Lat: %.5f, Lng: %.5f".format(lat, lng)
                binding.textLocationInfo.text = formatted
            }
        }
    }

    private fun updateLocationInfo(lat: Double, lng: Double, alt: Double, zoom: Double = 12.0) {
        val formatted = "Lat: %.5f, Lng: %.5f".format(lat, lng)
        binding.textLocationInfo.text = formatted
        lifecycleScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser(
                userName = sessionManager.akilimoUser
            )
            userRepository.saveOrUpdateUser(
                user.copy(
                    latitude = lat,
                    longitude = lng,
                    altitude = alt,
                    zoomLevel = zoom
                ), sessionManager.akilimoUser
            )
        }

    }


    private fun getCurrentLocation() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001
            )
            return
        }

        val provider = LocationManager.GPS_PROVIDER
        val location = locationManager.getLastKnownLocation(provider)
        if (location != null) {
            updateLocationInfo(location.latitude, location.longitude, location.altitude)
        } else {
            showToast("Unable to get current location")
        }
    }

}