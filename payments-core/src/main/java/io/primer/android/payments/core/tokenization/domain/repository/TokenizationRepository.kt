package io.primer.android.payments.core.tokenization.domain.repository

import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams

fun interface TokenizationRepository<T : BasePaymentInstrumentParams> {
    suspend fun tokenize(params: TokenizationParams<T>): Result<PaymentMethodTokenInternal>
}
