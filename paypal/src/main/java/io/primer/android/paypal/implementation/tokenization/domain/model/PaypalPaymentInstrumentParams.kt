package io.primer.android.paypal.implementation.tokenization.domain.model

import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalExternalPayerInfo
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalShippingAddressDataResponse

internal sealed interface PaypalPaymentInstrumentParams : BasePaymentInstrumentParams {

    class PaypalCheckoutPaymentInstrumentParams(
        val paypalOrderId: String?,
        val externalPayerInfoEmail: String?,
        val externalPayerId: String?,
        val externalPayerFirstName: String?,
        val externalPayerLastName: String?,
        override val paymentMethodType: String = PaymentMethodType.PAYPAL.name
    ) : PaypalPaymentInstrumentParams

    class PaypalVaultPaymentInstrumentParams(
        val paypalBillingAgreementId: String,
        val externalPayerInfo: PaypalExternalPayerInfo,
        val shippingAddress: PaypalShippingAddressDataResponse?,
        override val paymentMethodType: String = PaymentMethodType.PAYPAL.name
    ) : PaypalPaymentInstrumentParams
}
