package io.primer.android.otp.implementation.tokenization.data.mapper

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.otp.implementation.tokenization.data.model.AdyenBlikSessionInfoDataRequest
import io.primer.android.otp.implementation.tokenization.data.model.OtpPaymentInstrumentDataRequest
import io.primer.android.otp.implementation.tokenization.domain.model.OtpPaymentInstrumentParams
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams

internal class OtpTokenizationParamsMapper :
    TokenizationParamsMapper<OtpPaymentInstrumentParams, OtpPaymentInstrumentDataRequest> {
    override fun map(params: TokenizationParams<OtpPaymentInstrumentParams>):
        TokenizationRequestV2<OtpPaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest = OtpPaymentInstrumentDataRequest(
            paymentMethodType = paymentInstrumentParams.paymentMethodType,
            paymentMethodConfigId = paymentInstrumentParams.paymentMethodConfigId,
            sessionInfo = when (params.paymentInstrumentParams.paymentMethodType) {
                PaymentMethodType.ADYEN_BLIK.name -> AdyenBlikSessionInfoDataRequest(
                    locale = paymentInstrumentParams.locale,
                    blikCode = paymentInstrumentParams.otp
                )
                else -> error("Unsupported payment method type '${paymentInstrumentParams.paymentMethodType}'")
            },
            type = PaymentInstrumentType.OFF_SESSION_PAYMENT
        )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
