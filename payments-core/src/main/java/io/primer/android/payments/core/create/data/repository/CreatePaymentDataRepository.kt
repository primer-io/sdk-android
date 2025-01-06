package io.primer.android.payments.core.create.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.onError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.payments.core.create.data.datasource.CreatePaymentDataSource
import io.primer.android.payments.core.create.data.datasource.LocalPaymentDataSource
import io.primer.android.payments.core.create.data.model.CreatePaymentDataRequest
import io.primer.android.payments.core.create.data.model.toPaymentResult
import io.primer.android.payments.core.create.domain.repository.CreatePaymentRepository
import io.primer.android.payments.core.errors.data.exception.PaymentCreateException

internal class CreatePaymentDataRepository(
    private val createPaymentDataSource: CreatePaymentDataSource,
    private val localPaymentDataSource: LocalPaymentDataSource,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
) : CreatePaymentRepository {
    override suspend fun createPayment(token: String) =
        runSuspendCatching {
            configurationDataSource.get().let {
                createPaymentDataSource.execute(
                    BaseRemoteHostRequest(
                        host = it.pciUrl,
                        data = CreatePaymentDataRequest(token),
                    ),
                )
            }.let { paymentResponse ->
                localPaymentDataSource.update(paymentResponse)
                paymentResponse.toPaymentResult()
            }
        }.onError { throwable ->
            throw when {
                throwable is HttpException && (throwable.isClientError() || throwable.isPaymentError()) ->
                    PaymentCreateException(cause = throwable)

                else -> throwable
            }
        }
}
