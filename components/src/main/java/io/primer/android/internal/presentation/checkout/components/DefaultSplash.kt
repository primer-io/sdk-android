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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerLoading
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun DefaultSplash() {
    val theme = LocalPrimerTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PrimerLoading(
            accessibilityLabel = stringResource(R.string.accessibility_screen_loading_payment_methods),
        )

        Spacer(modifier = Modifier.height(theme.spacingTokens.small))

        Text(
            text = stringResource(R.string.primer_checkout_splash_title),
            color = theme.colorTokens().primerColorTextPrimary,
            style = theme.typographyTokens.bodyLarge.toTextStyle(),
        )

        Spacer(modifier = Modifier.height(theme.spacingTokens.xsmall))

        Text(
            text = stringResource(R.string.primer_checkout_splash_subtitle),
            color = theme.colorTokens().primerColorTextSecondary,
            style = theme.typographyTokens.bodyMedium.toTextStyle(),
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultSplashPreview() = PreviewContainer {
    DefaultSplash()
}
