package io.primer.android.domain.tokenization.repository

import io.primer.android.data.tokenization.models.TokenizationRequest
import io.primer.android.model.dto.PaymentMethodTokenInternal
import kotlinx.coroutines.flow.Flow

internal interface TokenizationRepository {

    fun tokenize(tokenizationRequest: TokenizationRequest): Flow<PaymentMethodTokenInternal>
}
