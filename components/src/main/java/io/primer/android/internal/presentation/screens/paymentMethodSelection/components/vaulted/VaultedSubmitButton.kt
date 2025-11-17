package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerButton

/**
 * Default pay button composable for vaulted payment methods.
 * Uses the same PrimerButton component as SubmitButton for consistency.
 */
@Composable
internal fun VaultedSubmitButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimerButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = LocalPrimerTheme.current.colorTokens().primerColorBrand,
        enabled = enabled && !isLoading,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = LocalPrimerTheme.current.colorTokens().primerColorBackground,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = stringResource(R.string.primer_components_pay),
                style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorBackground,
            )
        }
    }
}

/**
 * Preview of the pay button in normal state.
 */
@Preview(showBackground = true, name = "Pay Button - Normal")
@Composable
private fun DefaultPaymentVaultedSubmitButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Enabled:", style = MaterialTheme.typography.labelMedium)
            VaultedSubmitButton(
                enabled = true,
                isLoading = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Preview of the pay button in loading state.
 */
@Preview(showBackground = true, name = "Pay Button - Loading")
@Composable
private fun DefaultPaymentVaultedSubmitButtonLoadingPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Loading with spinner:", style = MaterialTheme.typography.labelMedium)
            VaultedSubmitButton(
                enabled = true,
                isLoading = true,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Preview of the pay button in disabled state.
 */
@Preview(showBackground = true, name = "Pay Button - Disabled")
@Composable
private fun DefaultPaymentVaultedSubmitButtonDisabledPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Disabled:", style = MaterialTheme.typography.labelMedium)
            VaultedSubmitButton(
                enabled = false,
                isLoading = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
