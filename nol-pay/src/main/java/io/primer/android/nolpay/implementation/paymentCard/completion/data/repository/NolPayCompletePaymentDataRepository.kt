package io.primer.android.nolpay.implementation.paymentCard.completion.data.repository

import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.extensions.runSuspendCatching

import io.primer.android.nolpay.implementation.paymentCard.completion.data.datasource.RemoteNolPayCompletePaymentDataSource
import io.primer.android.nolpay.implementation.common.domain.repository.NolPayCompletePaymentRepository

internal class NolPayCompletePaymentDataRepository(
    private val completePaymentDataSource: RemoteNolPayCompletePaymentDataSource
) : NolPayCompletePaymentRepository {
    override suspend fun completePayment(completeUrl: String) = runSuspendCatching {
        completePaymentDataSource.execute(
            completeUrl
        ).let { }
    }.recover { throwable: Throwable ->
        when (throwable) {
            is JsonDecodingException -> Unit
            else -> throw throwable
        }
    }
}
