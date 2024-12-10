package io.primer.android.klarna.implementation.session.domain.repository

import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam

internal fun interface KlarnaCustomerTokenRepository {

    suspend fun createCustomerToken(
        params: KlarnaCustomerTokenParam
    ): Result<CreateCustomerTokenDataResponse>
}
