package io.primer.android.domain.tokenization.repository

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.tokenization.models.TokenizationParams
import kotlinx.coroutines.flow.Flow

internal interface TokenizationRepository {

    fun tokenize(params: TokenizationParams): Flow<PaymentMethodTokenInternal>
}
