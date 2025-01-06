package io.primer.android.banks.implementation.tokenization.data.mapper

import io.primer.android.banks.implementation.tokenization.data.model.BankIssuerPaymentInstrumentDataRequest
import io.primer.android.banks.implementation.tokenization.data.model.BankIssuerSessionInfoDataRequest
import io.primer.android.banks.implementation.tokenization.domain.model.BankIssuerPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams

internal class BankIssuerTokenizationParamsMapper :
    TokenizationParamsMapper<BankIssuerPaymentInstrumentParams, BankIssuerPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<BankIssuerPaymentInstrumentParams>,
    ): TokenizationRequestV2<BankIssuerPaymentInstrumentDataRequest> {
        val instrumentDataRequest =
            BankIssuerPaymentInstrumentDataRequest(
                paymentMethodType = params.paymentInstrumentParams.paymentMethodType,
                paymentMethodConfigId = params.paymentInstrumentParams.paymentMethodConfigId,
                sessionInfo =
                    BankIssuerSessionInfoDataRequest(
                        redirectionUrl = params.paymentInstrumentParams.redirectionUrl,
                        locale = params.paymentInstrumentParams.locale,
                        issuer = params.paymentInstrumentParams.bankIssuer,
                    ),
                type = params.paymentInstrumentParams.type,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
