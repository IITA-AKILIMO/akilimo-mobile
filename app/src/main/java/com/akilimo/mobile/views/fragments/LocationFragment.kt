package com.akilimo.mobile.views.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresPermission
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentLocationBinding
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.services.GPSTracker
import com.akilimo.mobile.views.activities.MapBoxActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import com.stepstone.stepper.VerificationError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class LocationFragment : BindBaseStepFragment<FragmentLocationBinding>() {

    private var currentLat = 0.0
    private var currentLon = 0.0
    private var currentAlt = 0.0
    private var userSelectedCountryCode: String? = null
    private var userSelectedCountryName: String? = null

    private var countryCode: String = ""
    private var countryName: String = ""
    private var farmName: String? = ""
    private var fullNames: String? = null
    private var mapBoxToken = ""

    private var isLocationValid = false

    companion object {
        fun newInstance() = LocationFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentLocationBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        errorMessage = getString(R.string.lbl_location_error)
        mapBoxToken = sessionManager.getMapBoxApiKey()

        setupFarmNameDialog()
        setupLocationButtons()
        reloadLocationInfo()
    }

    private fun setupFarmNameDialog() {
        val editTextFarmName = EditText(context).apply {
            hint = getString(R.string.lbl_farm_name)
            isSingleLine = true
            maxLines = 1
        }

        val fieldNameDialog =
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.lbl_farm_name_title)
                .setView(editTextFarmName).setCancelable(false)
                .setPositiveButton(R.string.lbl_ok) { _, _ ->
                    farmName = editTextFarmName.text.toString()
                    if (fullNames != null) {
                        setFarmNameInfo(fullNames!!, farmName!!)
                    }
                    editTextFarmName.setText(farmName)
                }.setNegativeButton(R.string.lbl_cancel, null).create()

        binding.btnFieldName.setOnClickListener { fieldNameDialog.show() }

    }

    private fun setupLocationButtons() {
        val mapResultLauncher =
            registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        currentLat = data.getDoubleExtra(MapBoxActivity.LAT, 0.0)
                        currentLon = data.getDoubleExtra(MapBoxActivity.LON, 0.0)
                        currentAlt = data.getDoubleExtra(MapBoxActivity.ALT, 0.0)
                        reverseGeoCode(currentLat, currentLon)
                    } else {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

        binding.btnSelectLocation.setOnClickListener {
            val intent = Intent(requireContext(), MapBoxActivity::class.java).apply {
                putExtra(MapBoxActivity.LAT, currentLat)
                putExtra(MapBoxActivity.LON, currentLon)
                putExtra(MapBoxActivity.ALT, currentAlt)
            }
            mapResultLauncher.launch(intent)
        }

        binding.btnCurrentLocation.setOnClickListener { currentLocation }

    }

    private val currentLocation: Unit
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]) get() {
            val gps = GPSTracker(requireContext())
            gps.getLocation()

            if (gps.canGetLocation()) {
                val status = GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(requireContext())
                if (status == ConnectionResult.SUCCESS) {
                    currentLat = gps.latitudeValue
                    currentLon = gps.longitudeValue
                    gps.stopUsingGPS()
                    reverseGeoCode(currentLat, currentLon)
                } else {
                    showCustomWarningDialog(
                        "Google Play services not available on your phone",
                        "Google Play services unavailable"
                    )
                }
            } else {
                gps.showSettingsAlert()
            }
        }

    private fun reverseGeoCode(lat: Double, lon: Double) {
        val reverseGeocode = MapboxGeocoding.builder().accessToken(mapBoxToken)
            .query(Point.fromLngLat(lon, lat)).fuzzyMatch(true).build()

        reverseGeocode.enqueueCall(object : Callback<GeocodingResponse?> {
            override fun onResponse(
                call: Call<GeocodingResponse?>, response: Response<GeocodingResponse?>
            ) {
                if (!isAdded) return

                val features = response.body()?.features()
                if (!features.isNullOrEmpty()) {
                    val carmenFeature = features.last()
                    countryCode = carmenFeature.properties()
                        ?.get("short_code")?.asString?.uppercase(Locale.getDefault()).orEmpty()
                    countryName = carmenFeature.placeName().orEmpty()

                    val welcomeMessage = getString(R.string.location_info, countryName, lat, lon)
                    binding.locationInfo.text = welcomeMessage

                    isLocationValid = true
                    saveLocation()
                } else {
                    isLocationValid = false
                    showCustomWarningDialog("Unable to save location. Please pick a different location.")
                }
            }

            override fun onFailure(call: Call<GeocodingResponse?>, t: Throwable) {
                isLocationValid = false
                if (isAdded) {
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                }
                saveLocation()
            }
        })
    }

    private fun saveLocation() {
        try {
            val userProfile = database.profileInfoDao().findOne()
            if (userProfile != null) {
                userProfile.farmName = farmName.orEmpty()
                database.profileInfoDao().update(userProfile)
            }

            var locationInfo = database.locationInfoDao().findOne()
            if (locationInfo == null) locationInfo = UserLocation()

            locationInfo.apply {
                locationCountryCode = countryCode
                locationCountryName = countryName
                latitude = currentLat
                longitude = currentLon
            }

            if (locationInfo.id != null) {
                database.locationInfoDao().update(locationInfo)
            } else {
                database.locationInfoDao().insert(locationInfo)
            }

            reloadLocationInfo()
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun reloadLocationInfo() {
        try {
            val userProfile = database.profileInfoDao().findOne()
            val userLocation = database.locationInfoDao().findOne()

            userProfile?.let {
                farmName = it.farmName
                fullNames = "${it.firstName} ${it.lastName}"
                userSelectedCountryCode = it.countryCode
                userSelectedCountryName = it.countryName
                setFarmNameInfo(fullNames!!, farmName!!)
            }

            userLocation?.let {
                currentLat = it.latitude
                currentLon = it.longitude
                currentAlt = it.altitude
                countryCode = it.locationCountryCode.orEmpty()
                countryName = it.locationCountryName.orEmpty()
                binding.locationInfo.text = formatLocationInfo(it)
            }
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Unknown error"
        }
    }

    private fun setFarmNameInfo(fullNames: String, farmName: String) {
        val farmInfo =
            if (farmName.isEmpty() || fullNames.isEmpty()) null else "$fullNames’s cassava farm: $farmName"
        binding.txtFarmInfo.text = farmInfo
    }

    override fun verifyStep(): VerificationError? {
        return if (countryCode.isNotEmpty() && !userSelectedCountryCode.equals(
                countryCode,
                ignoreCase = true
            )
        ) {
            VerificationError(
                getString(R.string.lbl_unsupported_location, countryName, userSelectedCountryName)
            )
        } else null
    }

    override fun onSelected() {
        reloadLocationInfo()
    }
}