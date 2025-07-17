package io.primer.android.internal.presentation.screens.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.DefaultCardFormScreen() {
    LaunchedEffect(this) { init() }

    Column {
        CheckoutAppBar(
            title = stringResource(R.string.primer_components_select_payment_method_card),
            onBackClick = { onBack() },
            onCancelClick = { onCancel() },
        )
        Column(
            modifier = Modifier
                .padding(horizontal = LocalPrimerSpacingTokens.current.large),
        ) {
            cardDetails(Modifier)
            billingAddress(Modifier)
            Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.xsmall))
            submitButton(Modifier, stringResource(R.string.primer_components_card_form_pay))
        }
    }
}
