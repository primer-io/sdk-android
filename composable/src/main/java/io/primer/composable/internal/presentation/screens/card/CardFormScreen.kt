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
internal fun CardFormScope.CardFormScreen(
    modifier: Modifier = Modifier,
) {
    val spacing = LocalPrimerSpacingTokens.current

    Column {
        CheckoutAppBar(
            title = "Pay with card",
            onBackClick = { onBack() },
            onCancelClick = { onCancel() },
        )
        Column(
            modifier = modifier
                .padding(spacing.large),
        ) {
            CardDetailsForm()
            BillingAddressForm()
            SubmitButton(text = "Submit")
        }
    }
}
