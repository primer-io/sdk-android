package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam

internal interface KlarnaCustomerTokenRepository {

    suspend fun createCustomerToken(
        params: KlarnaCustomerTokenParam
    ): Result<CreateCustomerTokenDataResponse>
}
