package io.primer.composable.internal.presentation.screens.paymentMethodSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.composable.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.components.PaymentMethodSelector
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodSelectionScreen(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()
    val readyState = state as? PaymentMethodSelectionScope.State.Ready
    val paymentMethods = readyState?.paymentMethods ?: emptyList()
    val currency = readyState?.currency

    Column {
        CheckoutAppBar(
            title = "Select Payment Method",
            onCancelClick = { onCancel() },
        )

        LazyColumn(
            modifier = modifier.padding(horizontal = LocalPrimerSpacingTokens.current.large),
            verticalArrangement = Arrangement.spacedBy(LocalPrimerSpacingTokens.current.small),
        ) {
            item {
                Text(
                    text = "Choose payment method",
                    style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorTextPrimary,
                )
            }

            items(paymentMethods) { primerMethod ->
                PaymentMethodSelector(
                    primerMethod = primerMethod,
                    onPaymentMethodSelected = { onPaymentMethodSelected(primerMethod) },
                )
            }
        }
    }
}
