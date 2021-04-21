package io.primer.android.payment.google

import android.app.Activity
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.viewmodel.PaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistrar
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

internal abstract class InitialCheckRequiredBehaviour : SelectedPaymentMethodBehaviour() {

    abstract fun initialize(
        paymentMethodCheckerRegistrar: PaymentMethodCheckerRegistrar,
    )

    abstract fun execute(
        activity: Activity,
        tokenizationViewModel: TokenizationViewModel,
        googlePayBridge: GooglePayBridge,
    )
}

@KoinApiExtension
internal class GooglePayBehaviour constructor(
    private val paymentMethodDescriptor: GooglePayDescriptor,
    private val googlePayPaymentMethodChecker: PaymentMethodChecker,
) : InitialCheckRequiredBehaviour() {

    override fun initialize(paymentMethodCheckerRegistrar: PaymentMethodCheckerRegistrar) {
        paymentMethodCheckerRegistrar.register(
            GOOGLE_PAY_IDENTIFIER,
            googlePayPaymentMethodChecker
        )
    }

    override fun execute(
        activity: Activity,
        tokenizationViewModel: TokenizationViewModel,
        googlePayBridge: GooglePayBridge,
    ) {
        tokenizationViewModel.resetPaymentMethod(paymentMethodDescriptor)

        val paymentMethod = paymentMethodDescriptor.options
        val gatewayMerchantId = paymentMethodDescriptor.merchantId ?: return

        googlePayBridge.pay(
            activity = activity,
            gatewayMerchantId = gatewayMerchantId,
            merchantName = paymentMethod.merchantName,
            totalPrice = paymentMethod.totalPrice,
            countryCode = paymentMethod.countryCode,
            currencyCode = paymentMethod.currencyCode,
            allowedCardNetworks = paymentMethod.allowedCardNetworks,
            allowedCardAuthMethods = paymentMethod.allowedCardAuthMethods,
            billingAddressRequired = paymentMethod.billingAddressRequired
        )
    }
}
