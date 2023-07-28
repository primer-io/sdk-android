package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfo
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoParams
import kotlinx.coroutines.flow.Flow

internal interface PaypalInfoRepository {

    fun getPaypalOrderInfo(params: PaypalOrderInfoParams): Flow<PaypalOrderInfo>
}
