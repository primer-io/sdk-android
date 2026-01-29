package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.components.ui.assets.PrimerPaymentMethodAsset
import io.primer.android.components.ui.assets.PrimerPaymentMethodNativeView
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.internal.presentation.preview.mockPaymentMethods
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType.Companion.safeValueOf

/**
 * Default payment method item that automatically renders the appropriate UI
 * based on the payment method type and available assets.
 */
@Composable
internal fun DefaultPaymentMethodItem(
    primerMethod: PrimerComposablePaymentMethod,
    onPaymentMethodSelected: (String) -> Unit,
) {
    // Keep hardcoded implementations for specific payment methods
    when (safeValueOf(primerMethod.paymentMethodType)) {
        PaymentMethodType.PAYMENT_CARD -> {
            PaymentMethodItemCard(onPaymentMethodSelected)
        }

        PaymentMethodType.KLARNA -> {
            PaymentMethodItemKlarna(onPaymentMethodSelected)
        }

        else -> {
            // For other payment methods, use assets from AssetsManager
            val context = LocalContext.current
            val resource = remember(primerMethod.paymentMethodType) {
                runCatching {
                    PrimerHeadlessUniversalCheckoutAssetsManager
                        .getPaymentMethodResource(context, primerMethod.paymentMethodType)
                }.getOrNull()
            }

            when (resource) {
                is PrimerPaymentMethodNativeView -> PaymentMethodItemNative(
                    nativeView = resource,
                    onPaymentMethodSelected = onPaymentMethodSelected,
                )

                is PrimerPaymentMethodAsset -> PaymentMethodItemAsset(
                    asset = resource,
                    onPaymentMethodSelected = onPaymentMethodSelected,
                )

                null -> PaymentMethodItemComingSoon()
            }
        }
    }
}

/**
 * Base payment method item button with customizable styling.
 * Used as the foundation for all payment method items.
 */
@Composable
internal fun PaymentMethodItem(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    borderRadius: Dp = LocalPrimerTheme.current.radiusTokens.medium,
    backgroundColor: Color = LocalPrimerTheme.current.colorTokens().primerColorBackground,
    onPaymentMethodSelected: () -> Unit,
    accessibilityLabel: String? = null,
    content: @Composable () -> Unit,
) {
    PrimerButton(
        modifier = modifier,
        borderColor = borderColor,
        borderRadius = borderRadius,
        backgroundColor = backgroundColor,
        onClick = onPaymentMethodSelected,
        accessibilityLabel = accessibilityLabel,
    ) {
        content()
    }
}

@Preview(name = "Card Payment Method", showBackground = true)
@Composable
private fun DefaultPaymentMethodItemCardPreview() = PreviewContainer {
    DefaultPaymentMethodItem(
        primerMethod = mockPaymentMethods.first { it.paymentMethodType == "PAYMENT_CARD" },
        onPaymentMethodSelected = {},
    )
}

@Preview(name = "Klarna Payment Method", showBackground = true)
@Composable
private fun DefaultPaymentMethodItemKlarnaPreview() = PreviewContainer {
    DefaultPaymentMethodItem(
        primerMethod = mockPaymentMethods.first { it.paymentMethodType == "KLARNA" },
        onPaymentMethodSelected = {},
    )
}
