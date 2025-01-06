package io.primer.android.paypal.implementation.tokenization.data.mapper

import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.paypal.implementation.tokenization.data.model.ExternalPayerInfoRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalPaymentInstrumentDataRequest
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalPaymentInstrumentParams

internal class PaypalTokenizationParamsMapper :
    TokenizationParamsMapper<PaypalPaymentInstrumentParams, PaypalPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<PaypalPaymentInstrumentParams>,
    ): TokenizationRequestV2<PaypalPaymentInstrumentDataRequest> {
        val instrumentDataRequest =
            when (val paymentInstrumentParams = params.paymentInstrumentParams) {
                is PaypalPaymentInstrumentParams.PaypalCheckoutPaymentInstrumentParams ->
                    PaypalPaymentInstrumentDataRequest.PaypalCheckoutPaymentInstrumentDataRequest(
                        paypalOrderId = paymentInstrumentParams.paypalOrderId,
                        externalPayerInfo =
                            ExternalPayerInfoRequest(
                                email = paymentInstrumentParams.externalPayerInfoEmail,
                                externalPayerId = paymentInstrumentParams.externalPayerId,
                                firstName = paymentInstrumentParams.externalPayerFirstName,
                                lastName = paymentInstrumentParams.externalPayerLastName,
                            ),
                    )

                is PaypalPaymentInstrumentParams.PaypalVaultPaymentInstrumentParams ->
                    PaypalPaymentInstrumentDataRequest.PaypalVaultPaymentInstrumentDataRequest(
                        billingAgreementId = paymentInstrumentParams.paypalBillingAgreementId,
                        externalPayerInfo = paymentInstrumentParams.externalPayerInfo,
                        shippingAddress = paymentInstrumentParams.shippingAddress,
                    )
            }
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
