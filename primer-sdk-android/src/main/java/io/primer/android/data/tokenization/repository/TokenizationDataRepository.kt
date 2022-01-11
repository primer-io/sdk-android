package io.primer.android.data.tokenization.repository

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.data.tokenization.models.toTokenizationRequest
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import io.primer.android.model.Model
import kotlinx.coroutines.flow.Flow

internal class TokenizationDataRepository(private val model: Model) :
    TokenizationRepository {

    override fun tokenize(params: TokenizationParams): Flow<PaymentMethodTokenInternal> {
        return model.tokenize(params.toTokenizationRequest())
    }
}
