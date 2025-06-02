package io.primer.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodItem

@Composable
fun PrimerPaymentMethodItem(
    modifier: Modifier = Modifier,
    name: String,
    onSelect: () -> Unit,
) = PaymentMethodItem(modifier, name, onSelect)
