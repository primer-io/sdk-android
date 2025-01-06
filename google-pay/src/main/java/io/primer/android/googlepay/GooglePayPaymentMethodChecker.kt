package io.primer.android.googlepay

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodChecker

internal class GooglePayPaymentMethodChecker(
    private val googlePayFacade: GooglePayFacade,
) : PaymentMethodChecker {
    override suspend fun shouldPaymentMethodBeAvailable(paymentMethod: PaymentMethod): Boolean {
        val googlePay = paymentMethod as GooglePay
        return googlePayFacade.checkIfIsReadyToPay(
            allowedCardNetworks = googlePay.allowedCardNetworks.map { type: CardNetwork.Type -> type.name },
            allowedCardAuthMethods = googlePay.allowedCardAuthMethods,
            billingAddressRequired = googlePay.billingAddressRequired,
            existingPaymentMethodRequired = googlePay.existingPaymentMethodRequired,
        )
    }
}
