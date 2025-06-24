package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType.Companion.safeValueOf
import io.primer.composable.internal.presentation.components.PrimerButton
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerRadiusTokens
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodSelector(
    primerMethod: io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod,
    onPaymentMethodSelected: (String) -> Unit,
) {
    when (safeValueOf(primerMethod.paymentMethodType)) {
        PaymentMethodType.PAYMENT_CARD -> paymentMethodCard(Modifier, onPaymentMethodSelected)
        PaymentMethodType.ADYEN_IDEAL -> PaymentMethodItemIdeal { onPaymentMethodSelected(PaymentMethodType.ADYEN_IDEAL.name) }
        PaymentMethodType.GOOGLE_PAY -> PaymentMethodItemGooglePay { onPaymentMethodSelected(PaymentMethodType.GOOGLE_PAY.name) }
        PaymentMethodType.KLARNA -> PaymentMethodItemKlarna { onPaymentMethodSelected(PaymentMethodType.KLARNA.name) }
        PaymentMethodType.PAYPAL -> PaymentMethodItemPaypal { onPaymentMethodSelected(PaymentMethodType.PAYPAL.name) }
        else -> {
            // TODO Handle other payment methods
        }
    }
}

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItem(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    borderRadius: Dp = LocalPrimerRadiusTokens.current.medium,
    backgroundColor: Color = LocalPrimerColorTokens.current.primerColorBackground,
    onPaymentMethodSelected: () -> Unit,
    content: @Composable () -> Unit,
) {
    PrimerButton(
        modifier = modifier,
        borderColor = borderColor,
        borderRadius = borderRadius,
        backgroundColor = backgroundColor,
        onClick = onPaymentMethodSelected
    ) { content() }
}
