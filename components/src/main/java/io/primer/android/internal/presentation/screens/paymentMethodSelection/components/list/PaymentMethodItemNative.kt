package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import io.primer.android.components.ui.assets.PrimerPaymentMethodNativeView

/**
 * Renders a native Android view for payment methods that require platform-specific UI.
 * Used for payment methods like Google Pay that provide their own button views.
 */
@Composable
internal fun PaymentMethodItemNative(
    nativeView: PrimerPaymentMethodNativeView,
    onPaymentMethodSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accessibilityLabel = "Pay with ${nativeView.paymentMethodName}"

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .testTag("primer_payment_method_${nativeView.paymentMethodType.lowercase()}")
            .semantics { contentDescription = accessibilityLabel },
        factory = { context -> nativeView.createView(context) },
        update = { view ->
            view.setOnClickListener {
                onPaymentMethodSelected(nativeView.paymentMethodType)
            }
        },
    )
}
