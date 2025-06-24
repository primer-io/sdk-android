package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType.Companion.safeValueOf
import io.primer.composable.internal.presentation.components.PrimerButton
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerRadiusTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodSelector(
    primerMethod: io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod,
    onPaymentMethodSelected: () -> Unit,
) {
    when (safeValueOf(primerMethod.paymentMethodType)) {
        PaymentMethodType.PAYMENT_CARD -> paymentMethodCard(Modifier,onPaymentMethodSelected)

        // TODO COMPOSABLE check which one to use
        PaymentMethodType.ADYEN_IDEAL,
        PaymentMethodType.BUCKAROO_IDEAL,
        PaymentMethodType.MOLLIE_IDEAL,
        PaymentMethodType.PAY_NL_IDEAL,
        -> PaymentMethodItemIdeal { onPaymentMethodSelected() }

        PaymentMethodType.GOOGLE_PAY -> PaymentMethodItemGooglePay { onPaymentMethodSelected() }
        PaymentMethodType.KLARNA -> PaymentMethodItemKlarna { onPaymentMethodSelected() }
        PaymentMethodType.PAYPAL -> PaymentMethodItemPaypal { onPaymentMethodSelected() }

        else ->
            // TODO COMPOSABLE Handle other payment methods
            PaymentMethodItem(
                borderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedDefault,
                backgroundColor = LocalPrimerColorTokens.current.primerColorBackground,
                onPaymentMethodSelected = onPaymentMethodSelected,
            ) {
                Text(
                    text = primerMethod.paymentMethodName ?: "",
                    style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorTextPrimary,
                    modifier = Modifier.padding(LocalPrimerSpacingTokens.current.large),
                )
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
