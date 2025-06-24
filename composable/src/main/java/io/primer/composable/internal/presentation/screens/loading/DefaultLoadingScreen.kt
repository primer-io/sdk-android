package io.primer.composable.internal.presentation.screens.loading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.primer.composable.R
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens

@Composable
internal fun DefaultLoadingScreen(
    modifier: Modifier = Modifier,
    text: String? = null,
) {
    val spacing = LocalPrimerSpacingTokens.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = text ?: stringResource(R.string.primer_components_checkout_loading),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = spacing.small),
        )
    }
}
