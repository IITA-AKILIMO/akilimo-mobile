package com.akilimo.mobile.ui.screens.onboarding.steps

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.LocationPickerRoute
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.network.LocationHelper
import com.akilimo.mobile.utils.PermissionHelper
import kotlinx.coroutines.launch

@Composable
fun LocationStep(
    latitude: Double,
    longitude: Double,
    altitude: Double,
    zoomLevel: Double,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper() }
    val permissionHelper = remember { PermissionHelper() }

    var locationText by remember(latitude, longitude) {
        mutableStateOf(
            if (latitude != 0.0 || longitude != 0.0)
                "Lat: %.5f, Lng: %.5f".format(latitude, longitude)
            else "",
        )
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.all { it.value }) {
            scope.launch {
                when (val r = locationHelper.getCurrentLocation(context)) {
                    is LocationHelper.LocationResult.Success -> {
                        val loc = r.location
                        onEvent(OnboardingViewModel.Event.LocationUpdated(loc.latitude, loc.longitude, 12.0))
                        locationText = "Lat: %.5f, Lng: %.5f".format(loc.latitude, loc.longitude)
                    }
                    is LocationHelper.LocationResult.Error -> errorMsg = r.message
                    else -> errorMsg = context.getString(R.string.lbl_location_error)
                }
            }
        } else {
            errorMsg = context.getString(R.string.lbl_location_error)
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.lbl_farm_location), style = MaterialTheme.typography.headlineMedium)

        if (locationText.isNotBlank()) {
            Text(locationText, style = MaterialTheme.typography.bodyMedium)
        }
        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                errorMsg = null
                if (permissionHelper.hasLocationPermission(context)) {
                    scope.launch {
                        when (val r = locationHelper.getCurrentLocation(context)) {
                            is LocationHelper.LocationResult.Success -> {
                                val loc = r.location
                                onEvent(OnboardingViewModel.Event.LocationUpdated(loc.latitude, loc.longitude, 12.0))
                                locationText = "Lat: %.5f, Lng: %.5f".format(loc.latitude, loc.longitude)
                            }
                            is LocationHelper.LocationResult.Error -> errorMsg = r.message
                            else -> errorMsg = context.getString(R.string.lbl_location_error)
                        }
                    }
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.lbl_current_location))
        }

        OutlinedButton(
            onClick = {
                navController.navigate(LocationPickerRoute(latitude, longitude, zoomLevel))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.lbl_manual_location))
        }
    }
}
