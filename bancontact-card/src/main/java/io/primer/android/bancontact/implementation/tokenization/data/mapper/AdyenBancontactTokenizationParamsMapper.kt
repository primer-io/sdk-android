package io.primer.android.bancontact.implementation.tokenization.data.mapper

import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactPaymentInstrumentDataRequest
import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactSessionInfoDataRequest
import io.primer.android.bancontact.implementation.tokenization.domain.model.AdyenBancontactPaymentInstrumentParams
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams

internal class AdyenBancontactTokenizationParamsMapper :
    TokenizationParamsMapper<AdyenBancontactPaymentInstrumentParams, AdyenBancontactPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<AdyenBancontactPaymentInstrumentParams>,
    ): TokenizationRequestV2<AdyenBancontactPaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest =
            AdyenBancontactPaymentInstrumentDataRequest(
                number = paymentInstrumentParams.number,
                expirationMonth = paymentInstrumentParams.expirationMonth,
                expirationYear = paymentInstrumentParams.expirationYear,
                cardholderName = paymentInstrumentParams.cardholderName,
                paymentMethodType = paymentInstrumentParams.paymentMethodType,
                paymentMethodConfigId = paymentInstrumentParams.paymentMethodConfigId,
                sessionInfo =
                AdyenBancontactSessionInfoDataRequest(
                    locale = paymentInstrumentParams.locale,
                    redirectionUrl = paymentInstrumentParams.redirectionUrl,
                    userAgent = paymentInstrumentParams.userAgent,
                ),
                type = PaymentInstrumentType.CARD_OFF_SESSION_PAYMENT,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
