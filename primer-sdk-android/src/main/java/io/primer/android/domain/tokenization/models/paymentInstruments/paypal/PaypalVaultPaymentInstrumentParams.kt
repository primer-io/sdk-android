package io.primer.android.domain.tokenization.models.paymentInstruments.paypal

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalShippingAddressDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalExternalPayerInfo
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal class PaypalVaultPaymentInstrumentParams(
    val paypalBillingAgreementId: String,
    val externalPayerInfo: PaypalExternalPayerInfo,
    val shippingAddress: PaypalShippingAddressDataResponse?,
) : BasePaymentInstrumentParams(PaymentMethodType.PAYPAL.name)
