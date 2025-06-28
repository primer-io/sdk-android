package io.primer.android.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.primer.android.components.R
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemGooglePay() {
    PaymentMethodItem(
        borderRadius = Int.MAX_VALUE.dp,
        backgroundColor = LocalPrimerColorTokens.current.primerColorGray900,
        onPaymentMethodSelected = { onPaymentMethodSelected(PaymentMethodType.GOOGLE_PAY.name) },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_primer_google_pay),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
