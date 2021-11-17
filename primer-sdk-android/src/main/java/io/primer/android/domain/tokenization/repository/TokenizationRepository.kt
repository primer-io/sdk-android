package io.primer.android.domain.tokenization.repository

import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.model.dto.PaymentMethodTokenInternal
import kotlinx.coroutines.flow.Flow

internal interface TokenizationRepository {

    fun tokenize(params: TokenizationParams): Flow<PaymentMethodTokenInternal>
}
