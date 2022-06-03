package io.primer.android.data.payments.resume.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.create.models.toPaymentResult
import io.primer.android.data.payments.resume.datasource.ResumePaymentDataSource
import io.primer.android.data.payments.resume.models.ResumePaymentRequest
import io.primer.android.domain.payments.resume.respository.ResumePaymentsRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

internal class ResumePaymentDataRepository(
    private val resumePaymentDataSource: ResumePaymentDataSource,
    private val configurationDataSource: LocalConfigurationDataSource,
) : ResumePaymentsRepository {

    override fun resumePayment(id: String, token: String) = configurationDataSource.get()
        .flatMapLatest {
            resumePaymentDataSource.execute(
                BaseRemoteRequest(
                    it,
                    Pair(id, ResumePaymentRequest(token))
                )
            )
        }.mapLatest {
            it.toPaymentResult()
        }
}
