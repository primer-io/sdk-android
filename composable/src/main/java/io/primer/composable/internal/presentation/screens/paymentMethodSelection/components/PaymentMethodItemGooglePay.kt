package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodItemGooglePay(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        borderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedDefault,
        backgroundColor = LocalPrimerColorTokens.current.primerColorBackground,
        onPaymentMethodSelected = onPaymentMethodSelected,
    ) {
        Text(
            text = "*insert google pay design*",
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic
        )
    }
}
