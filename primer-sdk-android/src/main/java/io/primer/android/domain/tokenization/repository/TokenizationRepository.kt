package io.primer.android.domain.tokenization.repository

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import kotlinx.coroutines.flow.Flow

internal interface TokenizationRepository {

    fun tokenize(params: TokenizationParams): Flow<PaymentMethodTokenInternal>

    fun tokenize(params: TokenizationParamsV2): Flow<PaymentMethodTokenInternal>
}
