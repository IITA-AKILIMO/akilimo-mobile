package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * The IITA and CGIAR logo on a card of its own, in the theme's own colours. AKILIMO is an IITA product and IITA is a CGIAR
 * centre, so this sits low on a screen, never in the header, which belongs to AKILIMO itself.
 *
 * Two official files are used: the standard logo on the light theme and the **reversed** logo (white IITA wordmark, for dark
 * backgrounds) on the dark theme. The choice is made here from the app's own theme, not with a `-night` resource, because
 * the app's light/dark setting can differ from the phone's: a `-night` file would show a white wordmark on a light page.
 */
@Composable
fun PartnerLockup(modifier: Modifier = Modifier) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < DARK_BACKGROUND_LUMINANCE
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Image(
            painter = painterResource(if (darkTheme) R.drawable.logo_iita_cgiar_reversed else R.drawable.logo_iita_cgiar),
            contentDescription = stringResource(R.string.account_logo_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(AkilimoSpacing.xs),
        )
    }
}

private const val DARK_BACKGROUND_LUMINANCE = 0.5f
