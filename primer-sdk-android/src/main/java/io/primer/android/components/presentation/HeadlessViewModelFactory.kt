package io.primer.android.components.presentation

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.presentation.paymentMethods.nativeUi.googlepay.GooglePayHeadlessViewModel
import io.primer.android.components.presentation.paymentMethods.nativeUi.ipay88.IPay88HeadlessViewModel
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.KlarnaHeadlessViewModel
import io.primer.android.components.presentation.paymentMethods.nativeUi.paypal.PaypalCheckoutHeadlessViewModel
import io.primer.android.components.presentation.paymentMethods.nativeUi.paypal.PaypalVaultHeadlessViewModel
import io.primer.android.components.presentation.paymentMethods.nativeUi.webRedirect.AsyncPaymentMethodNativeUiHeadlessViewModel
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodType

internal class HeadlessViewModelFactory {

    internal fun getViewModel(
        componentActivity: ComponentActivity,
        paymentMethodImplementationType: PaymentMethodImplementationType,
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent
    ) = when {
        paymentMethodType == PaymentMethodType.GOOGLE_PAY.name -> ViewModelProvider(
            componentActivity,
            GooglePayHeadlessViewModel.Companion.Factory()
        )[paymentMethodType, GooglePayHeadlessViewModel::class.java]
        paymentMethodType == PaymentMethodType.PAYPAL.name -> when (sessionIntent) {
            PrimerSessionIntent.CHECKOUT -> ViewModelProvider(
                componentActivity,
                PaypalCheckoutHeadlessViewModel.Companion.Factory()
            )[paymentMethodType, PaypalCheckoutHeadlessViewModel::class.java]
            PrimerSessionIntent.VAULT -> ViewModelProvider(
                componentActivity,
                PaypalVaultHeadlessViewModel.Companion.Factory()
            )[paymentMethodType, PaypalVaultHeadlessViewModel::class.java]
        }
        paymentMethodType == PaymentMethodType.KLARNA.name -> ViewModelProvider(
            componentActivity,
            KlarnaHeadlessViewModel.Companion.Factory()
        )[paymentMethodType, KlarnaHeadlessViewModel::class.java]
        paymentMethodImplementationType == PaymentMethodImplementationType.IPAY88_SDK ->
            ViewModelProvider(
                componentActivity,
                IPay88HeadlessViewModel.Companion.Factory()
            )[paymentMethodType, IPay88HeadlessViewModel::class.java]
        else -> ViewModelProvider(
            componentActivity,
            AsyncPaymentMethodNativeUiHeadlessViewModel.Companion.Factory()
        )[paymentMethodType, AsyncPaymentMethodNativeUiHeadlessViewModel::class.java]
    }
}
