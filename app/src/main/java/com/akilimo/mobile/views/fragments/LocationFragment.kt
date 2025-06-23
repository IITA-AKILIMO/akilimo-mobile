package com.akilimo.mobile.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentLocationBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.repo.MapBoxLocationRepository
import com.akilimo.mobile.services.DefaultLocationProvider
import com.akilimo.mobile.viewmodels.LocationViewModel
import com.akilimo.mobile.viewmodels.factory.LocationViewModelFactory
import com.akilimo.mobile.views.activities.MapBoxActivity
import com.akilimo.mobile.views.fragments.dialog.FarmNameDialogFragment
import com.stepstone.stepper.VerificationError

class LocationFragment : BindBaseStepFragment<FragmentLocationBinding>() {

    private val viewModel: LocationViewModel by viewModels {
        val context = requireContext().applicationContext
        val repo = MapBoxLocationRepository(preferenceManager.mapBoxApiKey)
        val provider = DefaultLocationProvider(context)
        LocationViewModelFactory(requireActivity().application, repo, provider)
    }


    private var locationState = LocationState()

    private var userSelectedCountryCode: String? = null
    private var userSelectedCountryName: String? = null

    companion object {
        fun newInstance(): LocationFragment = LocationFragment()
    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentLocationBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        errorMessage = getString(R.string.lbl_location_error)

        setupObservers()
        setupFarmNameDialog()
        setupLocationButtons()
        viewModel.loadInitialData()
    }

    override fun onSelected() {
        viewModel.loadInitialData()
    }

    override fun setupObservers() {
        viewModel.farmName.observe(viewLifecycleOwner) { updateFarmInfo() }
        viewModel.fullName.observe(viewLifecycleOwner) { updateFarmInfo() }

        viewModel.locationInfo.observe(viewLifecycleOwner) { location ->
            location?.let {
                locationState = locationState.copy(
                    lat = it.latitude,
                    lon = it.longitude,
                    alt = it.altitude,
                    countryCode = it.locationCountryCode.orEmpty(),
                    countryName = it.locationCountryName.orEmpty(),
                    isValid = true
                )
                binding.textLocationInfo.text = formatLocationInfo(it)
            }
        }
    }

    private fun setupFarmNameDialog() {
        binding.btnSelectFarmName.setOnClickListener {
            val modal = FarmNameDialogFragment { farmName ->
                viewModel.saveFarmName(farmName)
            }
            modal.show(childFragmentManager, "FarmNameModal")
        }
    }


    private fun updateFarmInfo() {
        val full = viewModel.fullName.value.orEmpty()
        val farm = viewModel.farmName.value.orEmpty()
        binding.textFarmInfo.text =
            if (farm.isBlank() || full.isBlank()) null else "$full’s cassava farm: $farm"
    }

    private fun setupLocationButtons() {
        val mapResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let { data ->
                        val lat = data.getDoubleExtra(MapBoxActivity.LAT, 0.0)
                        val lon = data.getDoubleExtra(MapBoxActivity.LON, 0.0)
                        val alt = data.getDoubleExtra(MapBoxActivity.ALT, 0.0)

                        viewModel.reverseGeocodeAndSave(lat, lon, alt) {
                            showError(it)
                            locationState = locationState.copy(isValid = false)
                        }

                        locationState = locationState.copy(lat = lat, lon = lon, alt = alt)
                    } ?: showError()
                }
            }

        binding.btnSelectLocationManually.setOnClickListener {
            mapResultLauncher.launch(Intent(requireContext(), MapBoxActivity::class.java).apply {
                putExtra(MapBoxActivity.LAT, locationState.lat)
                putExtra(MapBoxActivity.LON, locationState.lon)
                putExtra(MapBoxActivity.ALT, locationState.alt)
            })
        }

        binding.btnUseCurrentLocation.setOnClickListener {
            viewModel.fetchAndSaveCurrentLocation {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String? = errorMessage) {
        Toast.makeText(requireContext(), message ?: "Unexpected error", Toast.LENGTH_SHORT).show()
    }


    override fun verifyStep(): VerificationError? {
        val userCode = userSelectedCountryCode?.trim()?.lowercase()
        val currentCode = locationState.countryCode.trim().lowercase()

        return if (currentCode.isNotEmpty() && userCode != null && userCode != currentCode) {
            VerificationError(
                getString(
                    R.string.lbl_unsupported_location,
                    locationState.countryName,
                    userSelectedCountryName
                )
            )
        } else null
    }


    private data class LocationState(
        val lat: Double = 0.0,
        val lon: Double = 0.0,
        val alt: Double = 0.0,
        val countryCode: String = "",
        val countryName: String = "",
        val isValid: Boolean = false
    )
}

