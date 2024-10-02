package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.google.GooglePayFacade

internal class GooglePayPaymentMethodChecker(
    private val googlePayFacade: GooglePayFacade
) : PaymentMethodChecker {

    override suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod
    ): Boolean {
        val googlePay = paymentMethod as GooglePay
        return googlePayFacade.checkIfIsReadyToPay(
            allowedCardNetworks = googlePay.allowedCardNetworks.map { type -> type.name },
            allowedCardAuthMethods = googlePay.allowedCardAuthMethods,
            billingAddressRequired = googlePay.billingAddressRequired,
            existingPaymentMethodRequired = googlePay.existingPaymentMethodRequired
        )
    }
}
