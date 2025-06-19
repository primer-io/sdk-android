package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType.Companion.safeValueOf
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerRadiusTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodSelector(
    primerMethod: io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod,
    onPaymentMethodSelected: () -> Unit,
) {
    when (safeValueOf(primerMethod.paymentMethodType)) {
        PaymentMethodType.PAYMENT_CARD -> PaymentMethodItemCard { onPaymentMethodSelected() }

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
    backgroundColor: Color = LocalPrimerColorTokens.current.primerColorBackground,
    onPaymentMethodSelected: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LocalPrimerSizeTokens.current.xxlarge)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(LocalPrimerRadiusTokens.current.medium),
            )
            .then(
                borderColor?.let {
                    Modifier.border(
                        width = 1.dp,
                        color = it,
                        shape = RoundedCornerShape(LocalPrimerRadiusTokens.current.medium),
                    )
                } ?: Modifier,
            )
            .clickable { onPaymentMethodSelected() },
        contentAlignment = Alignment.Center,
    ) { content() }
}
