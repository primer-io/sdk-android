package io.primer.android.components.data.payments.paymentMethods.nolpay.repository

import io.primer.android.components.data.payments.paymentMethods.nolpay.datasource.RemoteNolPayCompletePaymentDataSource
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayCompletePaymentRepository
import io.primer.android.extensions.runSuspendCatching
import org.json.JSONException

internal class NolPayCompletePaymentDataRepository(
    private val completePaymentDataSource: RemoteNolPayCompletePaymentDataSource
) : NolPayCompletePaymentRepository {
    override suspend fun completePayment(completeUrl: String) = runSuspendCatching {
        completePaymentDataSource.execute(
            completeUrl
        ).let { }
    }.recover { throwable: Throwable ->
        when (throwable) {
            is JSONException -> Unit
            else -> throw throwable
        }
    }
}
