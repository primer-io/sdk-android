package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.primer.composable.R
import io.primer.composable.internal.presentation.constants.PaymentMethodColors
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemPaypal(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        modifier = modifier,
        backgroundColor = PaymentMethodColors.paypalYellow,
        onPaymentMethodSelected = onPaymentMethodSelected,
    ) {
        Row {
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_paypal_icon),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_paypal_logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.padding(start = LocalPrimerSpacingTokens.current.xsmall),
            )
        }
    }
}
