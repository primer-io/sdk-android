package io.primer.android.card.implementation.tokenization.data.mapper

import io.primer.android.card.implementation.tokenization.data.model.CardPaymentInstrumentDataRequest
import io.primer.android.card.implementation.tokenization.domain.model.CardPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams

internal class CardTokenizationParamsMapper :
    TokenizationParamsMapper<CardPaymentInstrumentParams, CardPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<CardPaymentInstrumentParams>,
    ): TokenizationRequestV2<CardPaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest =
            CardPaymentInstrumentDataRequest(
                number = paymentInstrumentParams.number,
                expirationMonth = paymentInstrumentParams.expirationMonth,
                expirationYear = paymentInstrumentParams.expirationYear,
                cvv = paymentInstrumentParams.cvv,
                cardholderName = paymentInstrumentParams.cardholderName,
                preferredNetwork = paymentInstrumentParams.preferredNetwork,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
