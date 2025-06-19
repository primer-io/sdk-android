package io.primer.composable.internal.presentation.screens.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.composable.internal.presentation.screens.card.components.BillingAddressForm
import io.primer.composable.internal.presentation.screens.card.components.CardDetailsForm
import io.primer.composable.internal.presentation.screens.card.components.SubmitButton
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.CardFormScope

@Composable
internal fun DefaultCardFormScreen() {
    Column {
        CheckoutAppBar(
            title = "Pay with card",
            onBackClick = { /* TODO: Will be handled by scope in ScopeDefaults */ },
            onCancelClick = { /* TODO: Will be handled by scope in ScopeDefaults */ },
        )
        Column(
            modifier = Modifier
                .padding(LocalPrimerSpacingTokens.current.large),
        ) {
            // TODO: These will be updated to use scope parameters in ScopeDefaults
            // CardDetailsForm()
            // BillingAddressForm()
            // SubmitButton(text = "Submit")
        }
    }
}
