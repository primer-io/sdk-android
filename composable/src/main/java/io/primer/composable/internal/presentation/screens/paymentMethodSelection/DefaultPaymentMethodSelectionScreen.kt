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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.composable.R
import io.primer.composable.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.components.PaymentMethodSelector
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.internal.presentation.utils.CurrencyFormatter
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.DefaultPaymentMethodSelectionScreen() {
    when (state.collectAsStateWithLifecycle().value) {
        is PrimerPaymentMethodSelectionScope.State.Loading -> Loading()
        is PrimerPaymentMethodSelectionScope.State.Ready -> Ready()
        is PrimerPaymentMethodSelectionScope.State.Error -> Error()
    }
}

// TODO COMPOSABLE missing design
@Composable
private fun PrimerPaymentMethodSelectionScope.Loading() {
    Box(
        modifier = Modifier
            .padding(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PrimerPaymentMethodSelectionScope.Ready() {

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
                    text = stringResource(R.string.primer_components_payment_method_selection_description),
                    style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorTextPrimary,
                )
            }

            items(state.paymentMethods) { primerMethod ->
                PaymentMethodSelector(
                    primerMethod = primerMethod,
                    onPaymentMethodSelected = ::onPaymentMethodSelected,
                )
            }
        }
    }
}

// TODO COMPOSABLE missing design
@Composable
private fun PrimerPaymentMethodSelectionScope.Error() {
    Box(
        modifier = Modifier
            .padding(LocalPrimerSizeTokens.current.xxxlarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.primer_components_payment_methods_error),
            style = LocalPrimerTypographyTokens.current.bodyLarge.toTextStyle(),
            color = LocalPrimerColorTokens.current.primerColorTextPrimary,
        )
    }
}
