package io.primer.android.data.payments.create.repository

import io.primer.android.data.payments.create.datasource.LocalPaymentDataSource
import io.primer.android.data.payments.create.models.toPaymentResult
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.domain.payments.create.repository.PaymentResultRepository

internal class PaymentResultDataRepository(
    private val localPaymentDataSource: LocalPaymentDataSource
) : PaymentResultRepository {

    override fun getPaymentResult(): PaymentResult {
        return localPaymentDataSource.get().toPaymentResult()
    }
}
