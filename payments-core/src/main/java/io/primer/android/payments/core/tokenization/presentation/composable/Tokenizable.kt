package io.primer.android.payments.core.tokenization.presentation.composable

import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

interface Tokenizable<in I : TokenizationInputable> {

    suspend fun tokenize(input: I): Result<PrimerPaymentMethodTokenData>
}
