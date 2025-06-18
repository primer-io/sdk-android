package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType.Companion.safeValueOf
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodSelector(
    primerMethod: io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod,
    onPaymentMethodSelected: () -> Unit,
) {
    when (safeValueOf(primerMethod.paymentMethodType)) {
        PaymentMethodType.PAYMENT_CARD -> {
            PaymentMethodItemCard { onPaymentMethodSelected() }
        }

        PaymentMethodType.ADYEN_IDEAL,
        PaymentMethodType.BUCKAROO_IDEAL,
        PaymentMethodType.MOLLIE_IDEAL,
        PaymentMethodType.PAY_NL_IDEAL -> {
            PaymentMethodItemIdeal { onPaymentMethodSelected() }
        }

        PaymentMethodType.GOOGLE_PAY -> {
            PaymentMethodItemGooglePay { onPaymentMethodSelected() }
        }

        PaymentMethodType.KLARNA -> {
            PaymentMethodItemKlarna { onPaymentMethodSelected() }
        }

        PaymentMethodType.PAYPAL -> {
            PaymentMethodItemPaypal { onPaymentMethodSelected() }
        }

        else -> {
            // TODO: Handle other payment methods
            PaymentMethodItem(
                borderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedDefault,
                backgroundColor = LocalPrimerColorTokens.current.primerColorBackground,
                onPaymentMethodSelected = onPaymentMethodSelected
            ) {
                Text(
                    text = primerMethod.paymentMethodName ?: "",
                    style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorTextPrimary,
                    modifier = Modifier.padding(LocalPrimerSpacingTokens.current.large)
                )
            }
        }
    }
}
