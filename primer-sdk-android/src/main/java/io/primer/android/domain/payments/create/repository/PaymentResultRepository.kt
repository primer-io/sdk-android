package io.primer.android.domain.payments.create.repository

import io.primer.android.domain.payments.create.model.PaymentResult

internal interface PaymentResultRepository {

    fun getPaymentResult(): PaymentResult
}
