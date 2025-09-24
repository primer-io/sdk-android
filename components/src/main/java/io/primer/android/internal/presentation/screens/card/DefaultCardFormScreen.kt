package io.primer.android.internal.presentation.screens.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.android.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.DefaultCardFormScreen() {
    Column {
        CheckoutAppBar(
            title = stringResource(R.string.primer_components_select_payment_method_card),
            onBackClick = { onBack() },
            onCancelClick = { onCancel() },
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    start = LocalPrimerTheme.current.spacingTokens.large,
                    end = LocalPrimerTheme.current.spacingTokens.large,
                    bottom = LocalPrimerTheme.current.spacingTokens.large,
                ),
        ) {
            with(components) {
                cardDetails(Modifier)
                billingAddress(Modifier)
                Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.xsmall))
                submitButton(Modifier, stringResource(R.string.primer_components_card_form_pay))
            }
        }
    }
}
