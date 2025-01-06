package io.primer.android.payments.core.tokenization.data.mapper

import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams

interface TokenizationParamsMapper<T : BasePaymentInstrumentParams, U : BasePaymentInstrumentDataRequest> {
    fun map(params: TokenizationParams<T>): TokenizationRequestV2<U>
}
