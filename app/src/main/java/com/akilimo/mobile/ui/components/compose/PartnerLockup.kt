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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * The IITA and CGIAR logo on a card of its own. AKILIMO is an IITA product and IITA is a CGIAR centre, so this sits low on a
 * screen, never in the header, which belongs to AKILIMO itself. The image has a white background, so it sits on a white
 * card in the light and the dark theme alike.
 */
@Composable
fun PartnerLockup(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_iita_cgiar),
            contentDescription = stringResource(R.string.account_logo_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(AkilimoSpacing.xs),
        )
    }
}
