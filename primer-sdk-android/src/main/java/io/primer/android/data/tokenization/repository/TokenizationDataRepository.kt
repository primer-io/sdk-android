package io.primer.android.data.tokenization.repository

import io.primer.android.data.tokenization.models.TokenizationRequest
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import io.primer.android.model.Model
import io.primer.android.model.dto.PaymentMethodTokenInternal
import kotlinx.coroutines.flow.Flow

internal class TokenizationDataRepository(private val model: Model) :
    TokenizationRepository {

    override fun tokenize(tokenizationRequest: TokenizationRequest):
        Flow<PaymentMethodTokenInternal> {
        return model.tokenize(tokenizationRequest)
    }
}
