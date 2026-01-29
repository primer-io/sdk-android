package io.primer.android.internal.presentation.screens.klarna.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R

/**
 * Loading indicator shown while Klarna initializes.
 */
@Composable
internal fun KlarnaLoading(modifier: Modifier = Modifier) {
    val theme = LocalPrimerTheme.current
    val colorTokens = theme.colorTokens()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 300.dp)
            .padding(theme.spacingTokens.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = colorTokens.primerColorBrand,
        )

        Spacer(modifier = Modifier.height(theme.spacingTokens.large))

        Text(
            text = stringResource(R.string.primer_checkout_loading_indicator),
            style = theme.typographyTokens.titleLarge.toTextStyle(),
            color = colorTokens.primerColorTextPrimary,
        )
    }
}
