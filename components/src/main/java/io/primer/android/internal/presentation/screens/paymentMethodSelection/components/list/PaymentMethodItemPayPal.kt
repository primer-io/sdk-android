package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.constants.PaymentMethodColors
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemPaypal(
    modifier: Modifier = Modifier,
) {
    PaymentMethodItem(
        modifier = modifier,
        backgroundColor = PaymentMethodColors.paypalYellow,
        onPaymentMethodSelected = { onPaymentMethodSelected(PaymentMethodType.PAYPAL.name) },
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
                modifier = Modifier.padding(start = LocalPrimerTheme.current.spacingTokens.xsmall),
            )
        }
    }
}
