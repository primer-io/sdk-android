package io.primer.android.data.payments.create.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.create.datasource.CreatePaymentDataSource
import io.primer.android.data.payments.create.datasource.LocalPaymentDataSource
import io.primer.android.data.payments.create.models.CreatePaymentRequest
import io.primer.android.data.tokenization.helper.PrimerPaymentMethodDataHelper
import io.primer.android.domain.payments.create.repository.CreatePaymentRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

internal class CreatePaymentDataRepository(
    private val createPaymentDataSource: CreatePaymentDataSource,
    private val localPaymentDataSource: LocalPaymentDataSource,
    private val configurationDataSource: LocalConfigurationDataSource,
    private val paymentMethodDataHelper: PrimerPaymentMethodDataHelper
) : CreatePaymentRepository {

    override fun createPayment(token: String) = configurationDataSource.get().flatMapLatest {
        createPaymentDataSource.execute(BaseRemoteRequest(it, CreatePaymentRequest(token)))
    }.mapLatest {
        localPaymentDataSource.update(it)
        paymentMethodDataHelper.preparePaymentResult(it)
    }
}
