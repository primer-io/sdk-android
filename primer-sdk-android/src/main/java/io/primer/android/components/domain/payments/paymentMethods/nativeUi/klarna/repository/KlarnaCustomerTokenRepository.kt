package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import kotlinx.coroutines.flow.Flow

internal interface KlarnaCustomerTokenRepository {

    fun createCustomerToken(params: KlarnaCustomerTokenParam): Flow<CreateCustomerTokenDataResponse>
}
