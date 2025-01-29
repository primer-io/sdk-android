package io.primer.android.phoneNumber.implementation.tokenization.data.mapper

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.phoneNumber.implementation.tokenization.data.model.PhoneNumberPaymentInstrumentDataRequest
import io.primer.android.phoneNumber.implementation.tokenization.data.model.PhoneNumberSessionInfoDataRequest
import io.primer.android.phoneNumber.implementation.tokenization.domain.model.PhoneNumberPaymentInstrumentParams

internal class PhoneNumberTokenizationParamsMapper :
    TokenizationParamsMapper<PhoneNumberPaymentInstrumentParams, PhoneNumberPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<PhoneNumberPaymentInstrumentParams>,
    ): TokenizationRequestV2<PhoneNumberPaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest =
            PhoneNumberPaymentInstrumentDataRequest(
                paymentMethodType = paymentInstrumentParams.paymentMethodType,
                paymentMethodConfigId = paymentInstrumentParams.paymentMethodConfigId,
                sessionInfo =
                PhoneNumberSessionInfoDataRequest(
                    locale = paymentInstrumentParams.locale,
                    phoneNumber = paymentInstrumentParams.phoneNumber,
                ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
