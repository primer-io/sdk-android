package io.primer.android.stripe.ach.implementation.payment.confirmation.data.repository

import io.primer.android.core.data.model.BaseRemoteUrlRequest
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.stripe.ach.implementation.payment.confirmation.data.datasource.RemoteStripeAchCompletePaymentDataSource
import io.primer.android.stripe.ach.implementation.payment.confirmation.data.model.StripeAchCompletePaymentDataRequest
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.repository.StripeAchCompletePaymentRepository

internal class StripeAchCompletePaymentDataRepository(
    private val completePaymentDataSource: RemoteStripeAchCompletePaymentDataSource,
) : StripeAchCompletePaymentRepository {
    override suspend fun completePayment(
        completeUrl: String,
        mandateTimestamp: String,
        paymentMethodId: String?,
    ) = runSuspendCatching {
        completePaymentDataSource.execute(
            BaseRemoteUrlRequest(
                url = completeUrl,
                StripeAchCompletePaymentDataRequest(
                    mandateTimestamp = mandateTimestamp,
                    paymentMethodId = paymentMethodId,
                ),
            ),
        )
        Unit
    }
}
