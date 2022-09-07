package io.primer.android.data.payments.resume.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.resume.datasource.ResumePaymentDataSource
import io.primer.android.data.payments.resume.models.ResumePaymentDataRequest
import io.primer.android.data.tokenization.helper.PrimerPaymentMethodDataHelper
import io.primer.android.domain.payments.resume.respository.ResumePaymentsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
internal class ResumePaymentDataRepository(
    private val resumePaymentDataSource: ResumePaymentDataSource,
    private val configurationDataSource: LocalConfigurationDataSource,
    private val paymentMethodDataHelper: PrimerPaymentMethodDataHelper
) : ResumePaymentsRepository {

    override fun resumePayment(id: String, token: String) = configurationDataSource.get()
        .flatMapLatest {
            resumePaymentDataSource.execute(
                BaseRemoteRequest(
                    it,
                    Pair(id, ResumePaymentDataRequest(token))
                )
            )
        }.mapLatest {
            paymentMethodDataHelper.preparePaymentResult(it)
        }
}
