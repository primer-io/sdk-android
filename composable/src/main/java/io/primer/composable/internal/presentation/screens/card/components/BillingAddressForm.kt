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
import androidx.compose.ui.unit.dp
import io.primer.composable.internal.presentation.screens.card.components.input.AddressLine1Input
import io.primer.composable.internal.presentation.screens.card.components.input.AddressLine2Input
import io.primer.composable.internal.presentation.screens.card.components.input.CityInput
import io.primer.composable.internal.presentation.screens.card.components.input.CountryCodeInput
import io.primer.composable.internal.presentation.screens.card.components.input.FirstNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.LastNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.PostalCodeInput
import io.primer.composable.internal.presentation.screens.card.components.input.StateInput
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.BillingAddressForm(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()
    val billingInputFields = state.billingFields
    if (billingInputFields.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "BILLING ADDRESS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        CountryCodeInput(
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FirstNameInput(
                modifier = Modifier.weight(1f),
            )
            LastNameInput(
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AddressLine1Input(
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        AddressLine2Input(
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PostalCodeInput(
                modifier = Modifier.weight(1f),
            )
            CityInput(
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StateInput(
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
