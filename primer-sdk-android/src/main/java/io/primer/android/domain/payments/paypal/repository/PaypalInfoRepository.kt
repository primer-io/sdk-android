package io.primer.android.domain.payments.paypal.repository

import io.primer.android.domain.payments.paypal.models.PaypalOrderInfo
import io.primer.android.domain.payments.paypal.models.PaypalOrderInfoParams
import kotlinx.coroutines.flow.Flow

internal interface PaypalInfoRepository {

    fun getPaypalOrderInfo(params: PaypalOrderInfoParams): Flow<PaypalOrderInfo>
}
