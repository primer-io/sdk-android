package io.primer.android.data.payments.create.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.create.datasource.CreatePaymentsDataSource
import io.primer.android.data.payments.create.models.CreatePaymentRequest
import io.primer.android.data.payments.create.models.toPaymentResult
import io.primer.android.domain.payments.create.repository.CreatePaymentsRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

internal class CreatePaymentsDataRepository(
    private val createPaymentsDataSource: CreatePaymentsDataSource,
    private val configurationDataSource: LocalConfigurationDataSource,
) : CreatePaymentsRepository {

    override fun createPayment(token: String) = configurationDataSource.get().flatMapLatest {
        createPaymentsDataSource.execute(BaseRemoteRequest(it, CreatePaymentRequest(token)))
    }.mapLatest { it.toPaymentResult() }
}
