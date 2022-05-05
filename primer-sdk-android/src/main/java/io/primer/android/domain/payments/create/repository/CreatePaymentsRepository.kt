package io.primer.android.domain.payments.create.repository

import io.primer.android.domain.payments.create.model.PaymentResult
import kotlinx.coroutines.flow.Flow

internal interface CreatePaymentsRepository {

    fun createPayment(token: String): Flow<PaymentResult>
}
