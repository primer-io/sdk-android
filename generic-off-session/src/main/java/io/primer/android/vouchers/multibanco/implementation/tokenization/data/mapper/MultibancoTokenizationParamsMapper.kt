package io.primer.android.vouchers.multibanco.implementation.tokenization.data.mapper

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.model.MultibancoPaymentInstrumentDataRequest
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.model.MultibancoSessionInfoDataRequest
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.model.MultibancoPaymentInstrumentParams

internal class MultibancoTokenizationParamsMapper :
    TokenizationParamsMapper<MultibancoPaymentInstrumentParams, MultibancoPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<MultibancoPaymentInstrumentParams>,
    ): TokenizationRequestV2<MultibancoPaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest =
            MultibancoPaymentInstrumentDataRequest(
                paymentMethodType = paymentInstrumentParams.paymentMethodType,
                paymentMethodConfigId = paymentInstrumentParams.paymentMethodConfigId,
                sessionInfo =
                MultibancoSessionInfoDataRequest(
                    locale = paymentInstrumentParams.locale,
                ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
