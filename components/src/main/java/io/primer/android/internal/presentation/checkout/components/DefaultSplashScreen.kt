package io.primer.android.internal.presentation.checkout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerLoading
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.internal.presentation.theme.LocalPrimerTypographyTokens

@Composable
internal fun DefaultSplashScreen() {
    Column(
        modifier = Modifier.height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PrimerLoading()

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.small))

        Text(
            text = stringResource(R.string.primer_components_checkout_splash_title),
            color = LocalPrimerColorTokens.current.primerColorTextPrimary,
            style = LocalPrimerTypographyTokens.current.bodyLarge.toTextStyle()
        )

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.xsmall))

        Text(
            text = stringResource(R.string.primer_checkout_splash_subtitle),
            color = LocalPrimerColorTokens.current.primerColorTextSecondary,
            style = LocalPrimerTypographyTokens.current.bodyMedium.toTextStyle()
        )
    }
}
