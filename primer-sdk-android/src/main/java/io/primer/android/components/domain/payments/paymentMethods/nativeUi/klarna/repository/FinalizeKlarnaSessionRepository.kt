package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam

internal interface FinalizeKlarnaSessionRepository {

    suspend fun finalize(
        params: KlarnaCustomerTokenParam
    ): Result<FinalizeKlarnaSessionDataResponse>
}
