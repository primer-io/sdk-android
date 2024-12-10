package io.primer.android.payments.core.resume.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.onError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.payments.core.create.data.model.toPaymentResult
import io.primer.android.payments.core.errors.data.exception.PaymentResumeException
import io.primer.android.payments.core.resume.data.datasource.ResumePaymentDataSource
import io.primer.android.payments.core.resume.data.model.ResumePaymentDataRequest
import io.primer.android.payments.core.resume.domain.respository.ResumePaymentsRepository

internal class ResumePaymentDataRepository(
    private val resumePaymentDataSource: ResumePaymentDataSource,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
) : ResumePaymentsRepository {

    override suspend fun resumePayment(paymentId: String, resumeToken: String) = runSuspendCatching {
        configurationDataSource.get()
            .let {
                resumePaymentDataSource.execute(
                    BaseRemoteHostRequest(
                        host = it.pciUrl,
                        data = Pair(paymentId, ResumePaymentDataRequest(resumeToken))
                    )
                )
            }.toPaymentResult()
    }.onError { throwable ->
        throw when {
            throwable is HttpException && (throwable.isClientError() || throwable.isPaymentError()) ->
                PaymentResumeException(cause = throwable)

            else -> throwable
        }
    }
}
