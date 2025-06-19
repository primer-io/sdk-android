package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.primer.composable.R
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemIdeal(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        modifier = modifier,
        // TODO COMPOSABLE move this somewhere in constants
        backgroundColor = Color(0xFFCC0066),
        onPaymentMethodSelected = onPaymentMethodSelected,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_primer_ideal_logo),
            // TODO COMPOSABLE content description
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
