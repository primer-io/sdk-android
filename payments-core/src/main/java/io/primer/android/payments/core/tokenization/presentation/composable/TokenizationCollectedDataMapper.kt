package io.primer.android.payments.core.tokenization.presentation.composable

import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams

interface TokenizationCollectedDataMapper<in I : TokenizationInputable, O : BasePaymentInstrumentParams> {
    suspend fun mapTokenizationData(input: I): Result<TokenizationParams<O>>
}
