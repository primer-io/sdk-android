package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.surcharge.paymentMethodsList
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.android.internal.presentation.utils.CurrencyFormatter
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.DefaultPaymentMethodSelectionScreen() {
    val state = state.collectAsStateWithLifecycle().value as PrimerPaymentMethodSelectionScope.State.Ready

    Column {
        CheckoutAppBar(
            title = CurrencyFormatter.formatTitle(LocalContext.current, state.orderInfo),
            onCancelClick = { onCancel() },
        )
        LazyColumn(
            modifier = Modifier.padding(horizontal = LocalPrimerSpacingTokens.current.large),
            verticalArrangement = Arrangement.spacedBy(LocalPrimerSpacingTokens.current.small),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(vertical = LocalPrimerSpacingTokens.current.small),
                    text = stringResource(R.string.primer_components_payment_method_selection_description),
                    style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorTextPrimary,
                )
            }

            paymentMethodsList(state.paymentMethods, this@DefaultPaymentMethodSelectionScreen)
        }
    }
}
