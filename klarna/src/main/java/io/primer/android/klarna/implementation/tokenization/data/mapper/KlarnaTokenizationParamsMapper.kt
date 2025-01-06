package io.primer.android.klarna.implementation.tokenization.data.mapper

import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaCheckoutPaymentInstrumentDataRequest
import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaPaymentInstrumentDataRequest
import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaVaultPaymentInstrumentDataRequest
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaCheckoutPaymentInstrumentParams
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaPaymentInstrumentParams
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaVaultPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams

internal class KlarnaTokenizationParamsMapper :
    TokenizationParamsMapper<KlarnaPaymentInstrumentParams, KlarnaPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<KlarnaPaymentInstrumentParams>,
    ): TokenizationRequestV2<KlarnaPaymentInstrumentDataRequest> {
        val instrumentDataRequest =
            when (params.paymentInstrumentParams) {
                is KlarnaCheckoutPaymentInstrumentParams -> {
                    val paymentInstrumentParams =
                        params.paymentInstrumentParams as
                            KlarnaCheckoutPaymentInstrumentParams
                    KlarnaCheckoutPaymentInstrumentDataRequest(
                        klarnaAuthorizationToken = paymentInstrumentParams.klarnaAuthorizationToken,
                        sessionData = paymentInstrumentParams.sessionData,
                    )
                }

                is KlarnaVaultPaymentInstrumentParams -> {
                    val paymentInstrumentParams = params.paymentInstrumentParams as KlarnaVaultPaymentInstrumentParams
                    KlarnaVaultPaymentInstrumentDataRequest(
                        klarnaCustomerToken = paymentInstrumentParams.klarnaCustomerToken,
                        sessionData = paymentInstrumentParams.sessionData,
                    )
                }
            }
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
