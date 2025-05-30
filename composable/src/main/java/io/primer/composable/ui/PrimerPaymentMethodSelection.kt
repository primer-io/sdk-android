package io.primer.composable.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
import io.primer.composable.model.PrimerPaymentMethod

@Composable
fun PrimerPaymentMethodSelection(
    modifier: Modifier = Modifier,
    paymentMethods: List<PrimerPaymentMethod>,
    selectPaymentMethod: (PrimerPaymentMethod) -> Unit,
) {
    PaymentMethodSelectionScreen(modifier, paymentMethods, selectPaymentMethod)
}
