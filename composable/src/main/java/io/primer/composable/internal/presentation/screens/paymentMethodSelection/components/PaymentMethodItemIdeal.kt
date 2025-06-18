package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.primer.composable.R
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodItemIdeal(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        modifier = modifier,
        backgroundColor = Color(0xFFCC0066),
        onPaymentMethodSelected = onPaymentMethodSelected,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_primer_ideal_logo),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
