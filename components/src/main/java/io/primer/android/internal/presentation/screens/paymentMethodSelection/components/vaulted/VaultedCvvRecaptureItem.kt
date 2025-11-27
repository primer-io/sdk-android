@file:Suppress("UnusedPrivateMember")

package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerInput

@Composable
internal fun VaultedCvvRecaptureItem(
    cvvValue: String,
    onCvvChange: (String) -> Unit,
    showError: Boolean,
    enabled: Boolean,
    cvvLength: Int,
    modifier: Modifier = Modifier,
) {
    val theme = LocalPrimerTheme.current
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = null,
                tint = theme.colorTokens().primerColorGray900,
                modifier = Modifier.size(theme.sizeTokens.medium),
            )

            Text(
                text = stringResource(R.string.primer_components_cvv_secure_prompt),
                style = theme.typographyTokens.bodySmall.toTextStyle(),
                color = theme.colorTokens().primerColorTextSecondary,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.width(120.dp)) {
            PrimerInput(
                value = cvvValue,
                onValueChange = { newValue ->
                    // Apply essential input filtering for CVV (same approach as CvvInput)
                    var processedValue = newValue.filter { it in "0123456789" }
                    if (processedValue.length > cvvLength) {
                        processedValue = processedValue.take(cvvLength)
                    }
                    onCvvChange(processedValue)
                },
                placeholder = stringResource(R.string.primer_components_cvv),
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                error = if (showError) {
                    stringResource(R.string.primer_components_vault_cvv_error)
                } else {
                    null
                },
                forceLtrForNumbers = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VaultedCardCvvRecaptureItemPreview_Empty() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            VaultedCvvRecaptureItem(
                cvvValue = "",
                onCvvChange = {},
                showError = false,
                enabled = true,
                cvvLength = 3,
            )
        }
    }
}

@Preview(name = "CVV Recapture - With Value", showBackground = true)
@Composable
private fun VaultedCardCvvRecaptureItemPreview_WithValue() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            VaultedCvvRecaptureItem(
                cvvValue = "123",
                onCvvChange = {},
                showError = false,
                enabled = true,
                cvvLength = 3,
            )
        }
    }
}

@Preview(name = "CVV Recapture - Error State", showBackground = true)
@Composable
private fun VaultedCardCvvRecaptureItemPreview_Error() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            VaultedCvvRecaptureItem(
                cvvValue = "12",
                onCvvChange = {},
                showError = true,
                enabled = true,
                cvvLength = 3,
            )
        }
    }
}

@Preview(name = "CVV Recapture - Disabled", showBackground = true)
@Composable
private fun VaultedCardCvvRecaptureItemPreview_Disabled() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            VaultedCvvRecaptureItem(
                cvvValue = "123",
                onCvvChange = {},
                showError = false,
                enabled = false,
                cvvLength = 3,
            )
        }
    }
}
