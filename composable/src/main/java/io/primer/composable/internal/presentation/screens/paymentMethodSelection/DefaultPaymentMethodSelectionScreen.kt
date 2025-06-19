package io.primer.composable.internal.presentation.screens.paymentMethodSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.composable.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.components.PaymentMethodSelector
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun DefaultPaymentMethodSelectionScreen() {
    // TODO: Will be updated to use scope parameters in ScopeDefaults
    // Temporarily simplified for compilation
    Column {
        Text("Payment Method Selection (Default)")
    }
}

// TODO COMPOSABLE missing design
@Composable
private fun PaymentMethodSelectionScope.Loading() {
    Box(
        modifier = Modifier
            .padding(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PaymentMethodSelectionScope.Ready() {

    val state = state.collectAsStateWithLifecycle().value as PaymentMethodSelectionScope.State.Ready

    Column {
        CheckoutAppBar(
            title = state.title,
            onCancelClick = { onCancel() },
        )
        LazyColumn(
            modifier = Modifier.padding(horizontal = LocalPrimerSpacingTokens.current.large),
            verticalArrangement = Arrangement.spacedBy(LocalPrimerSpacingTokens.current.small),
        ) {
            item {
                Text(
                    // TODO COMPOSABLE extract string resource
                    text = "Choose payment method",
                    style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorTextPrimary,
                )
            }

            items(state.paymentMethods) { primerMethod ->
                PaymentMethodSelector(
                    primerMethod = primerMethod,
                    onPaymentMethodSelected = { onPaymentMethodSelected(primerMethod) },
                )
            }
        }
    }
}

// TODO COMPOSABLE missing design
@Composable
private fun PaymentMethodSelectionScope.Error() {
    Box(
        modifier = Modifier
            .padding(LocalPrimerSizeTokens.current.xxxlarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            // TODO COMPOSABLE extract string resource
            text = "Error loading payment methods",
            style = LocalPrimerTypographyTokens.current.bodyLarge.toTextStyle(),
            color = LocalPrimerColorTokens.current.primerColorTextPrimary,
        )
    }
}
