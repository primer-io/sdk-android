package io.primer.android.klarna.implementation.session.domain.repository

import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam

internal fun interface FinalizeKlarnaSessionRepository {

    suspend fun finalize(
        params: KlarnaCustomerTokenParam
    ): Result<FinalizeKlarnaSessionDataResponse>
}
