package io.primer.composable.internal.presentation.screens.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.primer.composable.R
import io.primer.composable.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.composable.internal.presentation.screens.card.components.BillingAddressForm
import io.primer.composable.internal.presentation.screens.card.components.CardDetailsForm
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.DefaultCardFormScreen() {
    Column {
        CheckoutAppBar(
            title = stringResource(R.string.primer_components_pay_with_card),
            onBackClick = { onBack() },
            onCancelClick = { onCancel() },
        )
        Column(
            modifier = Modifier
                .padding(horizontal = LocalPrimerSpacingTokens.current.large),
        ) {
            CardDetailsForm()
            BillingAddressForm()
            Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.xsmall))
            submitButton(Modifier, stringResource(R.string.submit))
        }
    }
}
