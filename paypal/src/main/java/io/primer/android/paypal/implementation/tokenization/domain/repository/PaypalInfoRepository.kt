package io.primer.android.paypal.implementation.tokenization.domain.repository

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfo
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfoParams

internal interface PaypalInfoRepository {
    suspend fun getPaypalOrderInfo(params: PaypalOrderInfoParams): Result<PaypalOrderInfo>
}
