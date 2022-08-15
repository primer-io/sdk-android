package io.primer.android.payment.google

import android.app.Activity
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.utils.PaymentUtils
import io.primer.android.viewmodel.PaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import io.primer.android.viewmodel.TokenizationViewModel

internal abstract class InitialCheckRequiredBehaviour : SelectedPaymentMethodBehaviour() {

    abstract fun initialize(paymentMethodCheckerRegistrar: PaymentMethodCheckerRegistry)

    abstract fun execute(activity: Activity, tokenizationViewModel: TokenizationViewModel)
}

internal class GooglePayBehaviour constructor(
    private val paymentMethodDescriptor: GooglePayDescriptor,
    private val googlePayPaymentMethodChecker: PaymentMethodChecker,
    private val googlePayFacade: GooglePayFacade,
) : InitialCheckRequiredBehaviour() {

    override fun initialize(paymentMethodCheckerRegistrar: PaymentMethodCheckerRegistry) {
        // FIXME this is not being called at the moment
        paymentMethodCheckerRegistrar.register(
            paymentMethodDescriptor.options.type,
            googlePayPaymentMethodChecker
        )
    }

    override fun execute(
        activity: Activity,
        tokenizationViewModel: TokenizationViewModel,
    ) {
        tokenizationViewModel.resetPaymentMethod(paymentMethodDescriptor)

        val paymentMethod = paymentMethodDescriptor.options
        val gatewayMerchantId = paymentMethodDescriptor.merchantId ?: return
        val amount = paymentMethodDescriptor.localConfig.settings.currentAmount
        val currency = java.util.Currency.getInstance(paymentMethod.currencyCode)
        val amountString = PaymentUtils.minorToAmount(amount, currency).toString()

        googlePayFacade.pay(
            activity = activity,
            gatewayMerchantId = gatewayMerchantId,
            merchantName = paymentMethod.merchantName,
            totalPrice = amountString,
            countryCode = paymentMethod.countryCode,
            currencyCode = paymentMethod.currencyCode,
            allowedCardNetworks = paymentMethod.allowedCardNetworks,
            allowedCardAuthMethods = paymentMethod.allowedCardAuthMethods,
            billingAddressRequired = paymentMethod.billingAddressRequired
        )
    }
}
