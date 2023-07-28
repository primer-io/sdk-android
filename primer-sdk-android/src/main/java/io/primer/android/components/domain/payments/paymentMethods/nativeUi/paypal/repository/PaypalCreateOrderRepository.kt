package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrder
import kotlinx.coroutines.flow.Flow

internal interface PaypalCreateOrderRepository {

    fun createOrder(params: PaypalCreateOrderParams): Flow<PaypalOrder>
}
