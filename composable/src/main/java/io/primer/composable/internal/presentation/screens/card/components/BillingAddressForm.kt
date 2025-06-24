package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.BillingAddressForm(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()
    val billingInputFields = state.billingFields
    if (billingInputFields.isEmpty()) return

    val spacing = LocalPrimerSpacingTokens.current

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        countryCodeInput(Modifier.fillMaxWidth())

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.large),
        ) {

            firstNameInput(Modifier.weight(1f))
            lastNameInput(Modifier.weight(1f))
        }

        addressLine1Input(Modifier.fillMaxWidth())

        addressLine2Input(Modifier.fillMaxWidth())

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            postalCodeInput(Modifier.weight(1f))
            cityInput(Modifier.weight(1f))
        }

        stateInput(Modifier.fillMaxWidth())
    }
}
