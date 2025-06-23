package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.BillingAddressForm(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()
    val billingInputFields = state.billingFields
//    if (billingInputFields.isEmpty()) return

    val spacing = LocalPrimerSpacingTokens.current

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "BILLING ADDRESS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = spacing.large),
        )

        countryCodeInput(Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(spacing.medium))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {

            firstNameInput(Modifier.weight(1f))
            lastNameInput(Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        addressLine1Input(Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(spacing.medium))

        addressLine2Input(Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(spacing.medium))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            postalCodeInput(Modifier.weight(1f))
            cityInput(Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        stateInput(Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(spacing.xlarge))
    }
}
