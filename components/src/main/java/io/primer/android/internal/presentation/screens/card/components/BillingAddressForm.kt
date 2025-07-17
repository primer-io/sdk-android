package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.BillingAddressForm(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()
    val billingInputFields = state.billingFields
    if (billingInputFields.isEmpty()) return

    val spacingSmall = LocalPrimerSpacingTokens.current.small

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        countryCodeInput(Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(spacingSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            firstNameInput(Modifier.weight(1f))
            Spacer(modifier = Modifier.width(spacingSmall))
            lastNameInput(Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(spacingSmall))
        addressLine1Input(Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(spacingSmall))
        addressLine2Input(Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(spacingSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            postalCodeInput(Modifier.weight(1f))
            Spacer(modifier = Modifier.width(spacingSmall))
            cityInput(Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(spacingSmall))
        stateInput(Modifier.fillMaxWidth())
    }
}
