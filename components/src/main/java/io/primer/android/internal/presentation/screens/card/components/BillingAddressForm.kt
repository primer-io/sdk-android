package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.BillingAddressForm(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()
    val billingInputFields = state.billingFields
    if (billingInputFields.isEmpty()) return

    val spacingSmall = LocalPrimerTheme.current.spacingTokens.small
    val billingSectionDescription = stringResource(R.string.primer_components_content_description_billing_section)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = billingSectionDescription
                heading()
            },
    ) {
        with(components) {
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
}
