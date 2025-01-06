package io.primer.android.payments.core.create.data.repository

import io.primer.android.payments.core.create.data.datasource.LocalPaymentDataSource
import io.primer.android.payments.core.create.data.model.toPaymentResult
import io.primer.android.payments.core.create.domain.model.PaymentResult
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository

class PaymentResultDataRepository(
    private val localPaymentDataSource: LocalPaymentDataSource,
) : PaymentResultRepository {
    override fun getPaymentResult(): PaymentResult {
        return localPaymentDataSource.get().toPaymentResult()
    }
}
