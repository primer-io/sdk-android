package io.primer.android.stripe.ach.implementation.tokenization.data.mapper

import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.stripe.ach.implementation.tokenization.data.model.StripeAchPaymentInstrumentDataRequest
import io.primer.android.stripe.ach.implementation.tokenization.data.model.StripeAchSessionInfoDataRequest
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams

internal class StripeAchTokenizationParamsMapper :
    TokenizationParamsMapper<StripeAchPaymentInstrumentParams, StripeAchPaymentInstrumentDataRequest> {

    override fun map(params: TokenizationParams<StripeAchPaymentInstrumentParams>):
        TokenizationRequestV2<StripeAchPaymentInstrumentDataRequest> {
        val instrumentDataRequest = StripeAchPaymentInstrumentDataRequest(
            paymentMethodType = params.paymentInstrumentParams.paymentMethodType,
            paymentMethodConfigId = params.paymentInstrumentParams.paymentMethodConfigId,
            sessionInfo = StripeAchSessionInfoDataRequest(locale = params.paymentInstrumentParams.locale),
            type = params.paymentInstrumentParams.type
        )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
