package com.akilimo.mobile.ui.screens.onboarding.steps

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumCoverageStatus
import com.akilimo.mobile.navigation.LocationPickerRoute
import com.akilimo.mobile.network.LocationHelper
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.utils.PermissionHelper
import kotlinx.coroutines.launch

private const val GPS_ZOOM = 12.0

/** Localised message for a failed location lookup; LocationHelper's own text is for logs only. */
private fun LocationHelper.LocationResult.problemMessage(context: Context): String? = when (this) {
    is LocationHelper.LocationResult.Success -> null
    LocationHelper.LocationResult.LocationDisabled -> context.getString(R.string.lbl_location_disabled)
    LocationHelper.LocationResult.PermissionDenied -> context.getString(R.string.lbl_location_permission_denied)
    is LocationHelper.LocationResult.Error -> context.getString(R.string.lbl_location_error)
}

@Composable
fun LocationStep(
    latitude: Double,
    longitude: Double,
    altitude: Double,
    zoomLevel: Double,
    country: EnumCountry,
    coverage: EnumCoverageStatus,
    error: String?,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper() }
    val permissionHelper = remember { PermissionHelper() }

    val hasLocation = latitude != 0.0 || longitude != 0.0
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    fun fetchLocation() {
        if (isLocating) return
        isLocating = true
        errorMsg = null
        scope.launch {
            val result = locationHelper.getCurrentLocation(context)
            isLocating = false
            if (result is LocationHelper.LocationResult.Success) {
                val loc = result.location
                onEvent(OnboardingViewModel.Event.LocationUpdated(loc.latitude, loc.longitude, GPS_ZOOM))
            } else {
                errorMsg = result.problemMessage(context)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        // Approximate (coarse) location is valid for placing the farm
        if (permissions.any { it.value }) {
            fetchLocation()
        } else {
            errorMsg = context.getString(R.string.lbl_location_permission_denied)
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.lbl_farm_location), style = MaterialTheme.typography.headlineMedium)

        if (hasLocation) {
            Text(
                stringResource(R.string.lbl_lat_lng_format, latitude, longitude),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        when {
            error != null -> Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            coverage == EnumCoverageStatus.OUTSIDE -> Text(
                stringResource(R.string.lbl_coverage_outside),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            coverage == EnumCoverageStatus.INSIDE -> Text(
                stringResource(R.string.lbl_coverage_inside),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                if (permissionHelper.hasLocationPermission(context)) {
                    fetchLocation()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            },
            enabled = !isLocating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLocating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.lbl_locating))
            } else {
                Text(stringResource(R.string.lbl_current_location))
            }
        }

        OutlinedButton(
            onClick = {
                navController.navigate(
                    LocationPickerRoute(
                        lat = latitude,
                        lon = longitude,
                        zoom = zoomLevel,
                        countryCode = country.takeIf { it != EnumCountry.Unsupported }?.name.orEmpty(),
                    )
                )
            },
            enabled = !isLocating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.lbl_manual_location))
        }
    }
}
