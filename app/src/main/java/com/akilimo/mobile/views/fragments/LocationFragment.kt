package com.akilimo.mobile.views.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentLocationBinding
import com.akilimo.mobile.entities.LocationInfo
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.services.GPSTracker
import com.akilimo.mobile.views.activities.MapBoxActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 * Use the [LocationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocationFragment : BaseStepFragment() {
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!


    private var currentLat = 0.0
    private var currentLon = 0.0
    private var currentAlt = 0.0
    private var userSelectedCountryCode: String? = null
    private var userSelectedCountryName: String? = null

    private var userProfile: UserProfile? = null
    private var locationInformation: LocationInfo? = null
    private var farmName: String? = ""
    private var fullNames: String? = null
    private var MAP_BOX_ACCESS_TOKEN = ""

    companion object {
        fun newInstance(): LocationFragment {
            return LocationFragment()
        }
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnCurrentLocation.setOnClickListener(View.OnClickListener { view1: View? ->
            currentLocation
        })


        val editTextFarmName = EditText(context)
        editTextFarmName.hint = getString(R.string.lbl_farm_name)
        editTextFarmName.isSingleLine = true
        editTextFarmName.maxLines = 1

        if (editTextFarmName.parent != null) {
            (editTextFarmName.parent as ViewGroup).removeView(editTextFarmName)
        }
        val fieldNameDialog =
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.lbl_farm_name_title))
                .setView(editTextFarmName).setCancelable(false).setPositiveButton(
                    getString(R.string.lbl_ok)
                ) { dialogInterface: DialogInterface?, i: Int ->
                    farmName = editTextFarmName.text.toString()
                    setFarmNameInfo(fullNames!!, farmName!!)
                    editTextFarmName.setText(farmName)
                }.setNegativeButton(getString(R.string.lbl_cancel), null).create()

        binding.btnFieldName.setOnClickListener { theView: View? -> fieldNameDialog.show() }
        val mStartForResult = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            try {
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
            } catch (ex: Exception) {
                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }
        }

        binding.btnSelectLocation.setOnClickListener { v: View? ->
            val intent = Intent(activity, MapBoxActivity::class.java)
            intent.putExtra(MapBoxActivity.LAT, currentLat)
            intent.putExtra(MapBoxActivity.LON, currentLon)
            intent.putExtra(MapBoxActivity.ALT, currentAlt)
            mStartForResult.launch(intent)
        }

        errorMessage = requireContext().getString(R.string.lbl_location_error)

        MAP_BOX_ACCESS_TOKEN = sessionManager.getMapBoxApiKey()
    }

    private fun setFarmNameInfo(fullNames: String, farmName: String) {
        val farmInfo = if (farmName.isEmpty() || fullNames.isEmpty()) null else String.format(
            "%s’s cassava farm: %s",
            fullNames,
            farmName
        )
        binding.txtFarmInfo.text = farmInfo
    }

    private val currentLocation: Unit
        get() {
            val gps = GPSTracker(requireContext())
            gps.getLocation()
            if (gps.canGetLocation()) {
                val status =
                    GoogleApiAvailability.getInstance()
                        .isGooglePlayServicesAvailable(requireContext())
                if (status == ConnectionResult.SUCCESS) {
                    currentLat = gps.getLatitude()
                    currentLon = gps.getLongitude()
                    gps.stopUsingGPS()
                    reverseGeoCode(currentLat, currentLon)
                } else {
                    showCustomWarningDialog(
                        "Google play services not available on your phone",
                        "Google Play services unavailable"
                    )
                }
            } else {
                gps.showSettingsAlert()
            }
        }


    private fun reverseGeoCode(lat: Double, lon: Double) {
        val reverseGeocode = MapboxGeocoding.builder().accessToken(MAP_BOX_ACCESS_TOKEN)
            .query(Point.fromLngLat(lon, lat))
            .fuzzyMatch(true) //                .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
            .build()

        //https://api.mapbox.com/geocoding/v5/{endpoint}/{longitude},{latitude}.json
        //https://api.mapbox.com/geocoding/v5/mapbox.places/39.326888,-3.384999.json?access_token=pk.eyJ1IjoibWFzZ2VlayIsImEiOiJjanp0bm43ZmwwNm9jM29udjJod3V6dzB1In0.MevkJtANWZ8Wl9abnLu1Uw
        reverseGeocode.enqueueCall(object : Callback<GeocodingResponse?> {
            override fun onResponse(
                call: Call<GeocodingResponse?>,
                response: Response<GeocodingResponse?>
            ) {
                if (response.body() != null) {
                    val features = response.body()!!.features()
                    if (!features.isEmpty()) {
                        val featureSize = features.size
                        val carmenFeature = features[featureSize - 1]
                        countryCode = carmenFeature.properties()!!["short_code"].asString.uppercase(
                            Locale.getDefault()
                        )
                        countryName = carmenFeature.placeName()

                        val welcomeMessage =
                            getString(R.string.location_info, countryName, lat, lon)
                        binding.locationInfo.text = welcomeMessage
                        saveLocation()
                    } else {
                        showCustomWarningDialog("Unable to save location please pick a different location")
                    }
                }
            }

            override fun onFailure(call: Call<GeocodingResponse?>, throwable: Throwable) {
                saveLocation()
                Toast.makeText(context, throwable.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveLocation() {
        try {
            if (userProfile != null) {
                userProfile!!.farmName = farmName
                database.profileInfoDao().update(userProfile!!)
            }
            if (locationInformation == null) {
                locationInformation = LocationInfo()
            }
            locationInformation!!.locationCountryCode = countryCode
            locationInformation!!.locationCountryName = countryName
            locationInformation!!.latitude = currentLat
            locationInformation!!.longitude = currentLon

            if (locationInformation!!.id != null) {
                database.locationInfoDao().update(locationInformation!!)
            } else {
                database.locationInfoDao().insert(locationInformation!!)
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
        reloadLocationInfo()
    }

    private fun reloadLocationInfo() {
        try {
            userProfile = database.profileInfoDao().findOne()
            locationInformation = database.locationInfoDao().findOne()

            if (userProfile != null) {
                farmName = userProfile!!.farmName
                fullNames = userProfile!!.names()
                userSelectedCountryCode = userProfile!!.countryCode
                userSelectedCountryName = userProfile!!.countryName
                setFarmNameInfo(fullNames!!, farmName!!)
            }
            if (locationInformation != null) {
                val locInfo = formatLocationInfo(locationInformation)
                currentLon = locationInformation!!.longitude
                currentLat = locationInformation!!.latitude
                currentAlt = locationInformation!!.altitude
                countryCode = locationInformation!!.locationCountryCode!!
                countryName = locationInformation!!.locationCountryName
                binding.locationInfo.text = locInfo
            }
        } catch (ex: Exception) {
            errorMessage = ex.message!!
        }
    }

    override fun verifyStep(): VerificationError? {
        reverseGeoCode(currentLat, currentLon)
        if (countryCode.isNotEmpty() && !userSelectedCountryCode.equals(
                countryCode,
                ignoreCase = true
            )
        ) {
            return VerificationError(
                String.format(
                    getString(R.string.lbl_unsupported_location),
                    countryName,
                    userSelectedCountryName
                )
            )
        }
        return null
    }

    override fun onSelected() {
        reloadLocationInfo()
    }
}
