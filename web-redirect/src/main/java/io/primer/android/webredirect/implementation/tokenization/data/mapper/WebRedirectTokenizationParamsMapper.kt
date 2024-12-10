package io.primer.android.webredirect.implementation.tokenization.data.mapper

import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.webredirect.implementation.tokenization.data.model.WebRedirectPaymentInstrumentDataRequest
import io.primer.android.webredirect.implementation.tokenization.data.model.WebRedirectSessionInfoDataRequest
import io.primer.android.webredirect.implementation.tokenization.domain.model.WebRedirectPaymentInstrumentParams

internal class WebRedirectTokenizationParamsMapper :
    TokenizationParamsMapper<WebRedirectPaymentInstrumentParams, WebRedirectPaymentInstrumentDataRequest> {

    override fun map(params: TokenizationParams<WebRedirectPaymentInstrumentParams>):
        TokenizationRequestV2<WebRedirectPaymentInstrumentDataRequest> {
        val instrumentDataRequest = WebRedirectPaymentInstrumentDataRequest(
            paymentMethodType = params.paymentInstrumentParams.paymentMethodType,
            paymentMethodConfigId = params.paymentInstrumentParams.paymentMethodConfigId,
            sessionInfo = WebRedirectSessionInfoDataRequest(
                redirectionUrl = params.paymentInstrumentParams.redirectionUrl,
                locale = params.paymentInstrumentParams.locale,
                platform = params.paymentInstrumentParams.platform
            ),

            type = params.paymentInstrumentParams.type
        )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
