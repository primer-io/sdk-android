package io.primer.android.internal.presentation.checkout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R

@Composable
internal fun DefaultLoadingScreen(
    text: String? = null,
) {
    Column(
        modifier = Modifier.height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            text = text ?: stringResource(R.string.primer_components_checkout_loading),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = LocalPrimerTheme.current.spacingTokens.small),
        )
    }
}
