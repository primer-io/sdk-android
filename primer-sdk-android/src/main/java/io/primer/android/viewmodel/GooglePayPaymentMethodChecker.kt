package io.primer.android.viewmodel

import io.primer.android.GooglePay
import io.primer.android.PaymentMethod
import io.primer.android.model.dto.ClientSession
import io.primer.android.payment.google.GooglePayBridge

internal class GooglePayPaymentMethodChecker constructor(
    private val googlePayBridge: GooglePayBridge,
) : PaymentMethodChecker {

    override suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod,
        clientSession: ClientSession,
    ): Boolean {
        val googlePay = paymentMethod as GooglePay
        return googlePayBridge.checkIfIsReadyToPay(
            allowedCardNetworks = googlePay.allowedCardNetworks,
            allowedCardAuthMethods = googlePay.allowedCardAuthMethods,
            billingAddressRequired = googlePay.billingAddressRequired
        )
    }
}
