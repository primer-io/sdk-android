package io.primer.android.payments.core.tokenization.presentation

import io.primer.android.core.extensions.flatMap
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.payments.core.tokenization.data.model.toPaymentMethodToken
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.payments.core.tokenization.presentation.composable.Tokenizable
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

/**
 * Tokenizes payment method data.
 */
abstract class PaymentMethodTokenizationDelegate<in I : TokenizationInputable, O : BasePaymentInstrumentParams>(
    private val tokenizationInteractor: TokenizationInteractor<O>
) : Tokenizable<I>, TokenizationCollectedDataMapper<I, O> {
    /**
     * Tokenizes the given [TokenizationInputable], returning [token data][PrimerPaymentMethodTokenData] received
     * from the backend.
     */
    override suspend fun tokenize(input: I): Result<PrimerPaymentMethodTokenData> {
        return mapTokenizationData(input)
            .flatMap { tokenizationParams: TokenizationParams<O> ->
                tokenizationInteractor.invoke(tokenizationParams).map { it.toPaymentMethodToken() }
            }
    }
}
