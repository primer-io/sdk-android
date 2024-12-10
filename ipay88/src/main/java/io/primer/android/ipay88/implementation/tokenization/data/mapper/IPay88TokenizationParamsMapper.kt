package io.primer.android.ipay88.implementation.tokenization.data.mapper

import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.ipay88.implementation.tokenization.data.model.IPay88PaymentInstrumentDataRequest
import io.primer.android.ipay88.implementation.tokenization.data.model.IPay88SessionInfoDataRequest
import io.primer.android.ipay88.implementation.tokenization.domain.model.IPay88PaymentInstrumentParams

internal class IPay88TokenizationParamsMapper :
    TokenizationParamsMapper<IPay88PaymentInstrumentParams, IPay88PaymentInstrumentDataRequest> {

    override fun map(params: TokenizationParams<IPay88PaymentInstrumentParams>):
        TokenizationRequestV2<IPay88PaymentInstrumentDataRequest> {
        val instrumentDataRequest = IPay88PaymentInstrumentDataRequest(
            paymentMethodType = params.paymentInstrumentParams.paymentMethodType,
            paymentMethodConfigId = params.paymentInstrumentParams.paymentMethodConfigId,
            sessionInfo = IPay88SessionInfoDataRequest(
                params.paymentInstrumentParams.locale
            ),
            type = params.paymentInstrumentParams.type
        )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
