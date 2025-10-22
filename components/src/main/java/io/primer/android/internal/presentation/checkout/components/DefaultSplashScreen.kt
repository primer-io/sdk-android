package io.primer.android.internal.presentation.checkout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerLoading

@Composable
internal fun DefaultSplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PrimerLoading()

        Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.small))

        Text(
            text = stringResource(R.string.primer_components_checkout_splash_title),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
            style = LocalPrimerTheme.current.typographyTokens.bodyLarge.toTextStyle(),
        )

        Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))

        Text(
            text = stringResource(R.string.primer_checkout_splash_subtitle),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextSecondary,
            style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
        )
    }
}
