package io.primer.android.components.data.payments.paymentMethods.stripe.ach.repository

import io.primer.android.components.data.payments.paymentMethods.stripe.ach.datasource.RemoteStripeAchCompletePaymentDataSource
import io.primer.android.components.data.payments.paymentMethods.stripe.ach.model.StripeAchCompletePaymentDataRequest
import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.repository.StripeAchCompletePaymentRepository
import io.primer.android.data.base.models.BaseRemoteUrlRequest
import io.primer.android.extensions.runSuspendCatching

internal class StripeAchCompletePaymentDataRepository(
    private val completePaymentDataSource: RemoteStripeAchCompletePaymentDataSource
) : StripeAchCompletePaymentRepository {
    override suspend fun completePayment(
        completeUrl: String,
        mandateTimestamp: String,
        paymentMethodId: String?
    ) = runSuspendCatching {
        completePaymentDataSource.execute(
            BaseRemoteUrlRequest(
                url = completeUrl,
                StripeAchCompletePaymentDataRequest(
                    mandateTimestamp = mandateTimestamp,
                    paymentMethodId = paymentMethodId
                )
            )
        )
        Unit
    }
}
