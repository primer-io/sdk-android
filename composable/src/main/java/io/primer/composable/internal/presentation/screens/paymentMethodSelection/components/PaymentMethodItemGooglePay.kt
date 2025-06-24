package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.primer.composable.R
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemGooglePay(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        borderRadius = Int.MAX_VALUE.dp,
        backgroundColor = LocalPrimerColorTokens.current.primerColorGray900,
        onPaymentMethodSelected = onPaymentMethodSelected,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_primer_google_pay),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
