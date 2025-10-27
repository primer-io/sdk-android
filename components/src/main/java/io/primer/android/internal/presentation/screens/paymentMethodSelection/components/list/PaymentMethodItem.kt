package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType.Companion.safeValueOf
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.DefaultPaymentMethodItem(
    primerMethod: PrimerComposablePaymentMethod,
) {
    with(components) {
        when (safeValueOf(primerMethod.paymentMethodType)) {
            PaymentMethodType.PAYMENT_CARD -> PaymentMethodItemCard()
            PaymentMethodType.ADYEN_IDEAL -> PaymentMethodItemIdeal()
            PaymentMethodType.GOOGLE_PAY -> PaymentMethodItemGooglePay()
            PaymentMethodType.KLARNA -> PaymentMethodItemKlarna()
            PaymentMethodType.PAYPAL -> PaymentMethodItemPaypal()
            else -> { PaymentMethodItemComingSoon { } }
        }
    }
}

@Composable
internal fun PaymentMethodItem(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    borderRadius: Dp = LocalPrimerTheme.current.radiusTokens.medium,
    backgroundColor: Color = LocalPrimerTheme.current.colorTokens().primerColorBackground,
    onPaymentMethodSelected: () -> Unit,
    content: @Composable () -> Unit,
) {
    PrimerButton(
        modifier = modifier,
        borderColor = borderColor,
        borderRadius = borderRadius,
        backgroundColor = backgroundColor,
        onClick = onPaymentMethodSelected,
    ) { content() }
}
