package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.components.ui.assets.PrimerPaymentMethodNativeView
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemGooglePay() {
    val context = LocalContext.current
    val nativeView = remember {
        PrimerHeadlessUniversalCheckoutAssetsManager
            .getPaymentMethodResource(context, PaymentMethodType.GOOGLE_PAY.name) as PrimerPaymentMethodNativeView
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { nativeView.createView(it) },
        update = { it.setOnClickListener { onPaymentMethodSelected(PaymentMethodType.GOOGLE_PAY.name) } },
    )
}
