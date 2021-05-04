package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.ClientSession
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.google.GooglePayFacade

internal class GooglePayPaymentMethodChecker constructor(
    private val googlePayFacade: GooglePayFacade,
) : PaymentMethodChecker {

    override suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod,
        clientSession: ClientSession,
    ): Boolean {
        val googlePay = paymentMethod as GooglePay
        return googlePayFacade.checkIfIsReadyToPay(
            allowedCardNetworks = googlePay.allowedCardNetworks,
            allowedCardAuthMethods = googlePay.allowedCardAuthMethods,
            billingAddressRequired = googlePay.billingAddressRequired
        )
    }
}
